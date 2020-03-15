package io.netlibs.zzz.aws.arn;

import org.immutables.value.Value;

@Value.Immutable
public interface DynamoDBStreamArn extends ArnUri {

  String tableName();

  String streamName();

  static DynamoDBStreamArn fromString(String arn) {
    return ArnParser.parse(arn, DynamoDBStreamArn.class);
  }

}