package io.netlibs.zzz.aws.arn;

import org.immutables.value.Value;

@Value.Immutable
public interface DynamoDBTableArn extends ArnUri {

  String tableName();

  static DynamoDBTableArn fromString(String arn) {
    return ArnParser.parse(arn, DynamoDBTableArn.class);
  }

}