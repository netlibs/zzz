package io.netlibs.zzz.aws.arn;

import org.immutables.value.Value;

// arn:aws:execute-api:us-west-2:123456789012:example/prod/POST/{proxy+}
// arn:${Partition}:execute-api:${Region}:${Account}:${ApiId}/${Stage}/${Method}/${ApiSpecificResourcePath}

@Value.Immutable
public interface ExecuteApiArn extends ArnUri {

  String apiId();

  String stage();

  String method();

  String apiSpecificResourcePath();

  static ExecuteApiArn fromString(String arn) {
    return ArnParser.parse(arn, ExecuteApiArn.class);
  }

}