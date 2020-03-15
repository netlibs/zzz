package io.netlibs.zzz.aws.arn;

import org.immutables.value.Value;

@Value.Immutable
public interface KinesisStreamArn extends ArnUri {

  String streamName();

  static KinesisStreamArn fromString(String arn) {
    return ArnParser.parse(arn, KinesisStreamArn.class);
  }

}