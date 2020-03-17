package io.netlibs.zzz.aws.arn;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;

public class ArnParser {

  public static ArnUri parse(String input) {
    Verify.verify(input.startsWith("arn:"));
    return create(ImmutableList.copyOf(Splitter.on(':').limit(6).split(input)));
  }

  // arn:partition:service:region:account-id:resource
  // arn:partition:service:region:account-id:resourcetype/resource
  // arn:partition:service:region:account-id:resourcetype/resource/qualifier
  // arn:partition:service:region:account-id:resourcetype/resource:qualifier
  // arn:partition:service:region:account-id:resourcetype:resource
  // arn:partition:service:region:account-id:resourcetype:resource:qualifier

  private static class UnknownArnUri implements ArnUri {

    private ImmutableList<String> components;

    protected UnknownArnUri(ImmutableList<String> components) {
      this.components = components;
    }

    @Override
    public String toString() {
      return Joiner.on(':').join(components);
    }

    @Override
    public String partition() {
      return components.get(1);
    }

    @Override
    public String service() {
      return components.get(2);
    }

    @Override
    public String region() {
      return components.get(3);
    }

    @Override
    public String accountId() {
      return components.get(4);
    }

    @Override
    public String resource() {
      return components.get(5);
    }

    @Override
    public String toArn() {
      return toString();
    }

  }

  private static ArnUri create(ImmutableList<String> components) {

    Verify.verify(components.size() == 6);

    switch (components.get(2)) {
      case "dynamodb":
        return createDynamoDB(components);
      case "kinesis":
        return createKinesis(components);
      case "execute-api":
        return createExecuteApi(components);
      case "rds":
        return createRds(components);
      default:
        return new UnknownArnUri(components);
    }

  }

  private static ArnUri createExecuteApi(ImmutableList<String> components) {

    // arn:${Partition}:execute-api:${Region}:${Account}:${ApiId}/${Stage}/${Method}/${ApiSpecificResourcePath}

    ImmutableList<String> parts = ImmutableList.copyOf(Splitter.on('/').limit(4).split(components.get(5)));

    Verify.verify((parts.size() == 4), "", parts.size(), parts);

    return ImmutableExecuteApiArn.builder()
      .partition(components.get(1))
      .service(components.get(2))
      .region(components.get(3))
      .accountId(components.get(4))
      .resource(components.get(5))
      .apiId(parts.get(0))
      .stage(parts.get(1))
      .method(parts.get(2))
      .apiSpecificResourcePath(parts.get(3))
      .build();

  }

  private static ArnUri createDynamoDB(ImmutableList<String> components) {

    ImmutableList<String> parts = ImmutableList.copyOf(Splitter.on('/').limit(4).split(components.get(5)));

    Verify.verify((parts.size() == 2) || (parts.size() == 4), "", parts.size(), parts);

    switch (parts.size()) {
      case 2:
        if (parts.get(0).contentEquals("table")) {
          return ImmutableDynamoDBTableArn.builder()
            .partition(components.get(1))
            .service(components.get(2))
            .region(components.get(3))
            .accountId(components.get(4))
            .resource(components.get(5))
            .tableName(parts.get(1))
            .build();
        }
        break;
      case 4:
        if (parts.get(0).contentEquals("table") && parts.get(2).contentEquals("stream")) {
          return ImmutableDynamoDBStreamArn.builder()
            .partition(components.get(1))
            .service(components.get(2))
            .region(components.get(3))
            .accountId(components.get(4))
            .resource(components.get(5))
            .tableName(parts.get(1))
            .streamName(parts.get(3))
            .build();
        }

    }

    return new UnknownArnUri(components);

  }

  private static ArnUri createKinesis(ImmutableList<String> components) {

    ImmutableList<String> parts = ImmutableList.copyOf(Splitter.on('/').limit(4).split(components.get(5)));

    Verify.verify((parts.size() == 2) || (parts.size() == 4), "", parts.size(), parts);

    switch (parts.size()) {
      case 2:
        if (parts.get(0).contentEquals("stream")) {
          return ImmutableKinesisStreamArn.builder()
            .partition(components.get(1))
            .service(components.get(2))
            .region(components.get(3))
            .accountId(components.get(4))
            .resource(components.get(5))
            .streamName(parts.get(1))
            .build();
        }

        break;
      case 4:
        if (parts.get(0).contentEquals("stream") && parts.get(2).contentEquals("consumer")) {
          return ImmutableKinesisStreamConsumerArn.builder()
            .partition(components.get(1))
            .service(components.get(2))
            .region(components.get(3))
            .accountId(components.get(4))
            .resource(components.get(5))
            .streamName(parts.get(1))
            .consumerName(parts.get(3))
            .build();
        }

    }

    return new UnknownArnUri(components);

  }

  private static ArnUri createRds(ImmutableList<String> components) {

    ImmutableList<String> parts = ImmutableList.copyOf(Splitter.on(':').limit(2).split(components.get(5)));

    switch (parts.get(0)) {
      case "cluster":
        Verify.verify(parts.size() == 2);
        return ImmutableRdsClusterArn.builder()
          .partition(components.get(1))
          .service(components.get(2))
          .region(components.get(3))
          .accountId(components.get(4))
          .resource(components.get(5))
          .clusterId(parts.get(1))
          .build();
      case "db":
        Verify.verify(parts.size() == 2);
        return ImmutableRdsDbArn.builder()
          .partition(components.get(1))
          .service(components.get(2))
          .region(components.get(3))
          .accountId(components.get(4))
          .resource(components.get(5))
          .instanceId(parts.get(1))
          .build();
    }

    return new UnknownArnUri(components);

  }

  public static <T extends ArnUri> T parse(String string, Class<T> type) {
    return type.cast(parse(string));
  }

}
