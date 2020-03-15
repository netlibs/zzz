package io.netlibs.zzz.aws.arn;

import org.immutables.value.Value;

@Value.Immutable
public interface KinesisStreamConsumerArn extends ArnUri {

  String streamName();

  String consumerName();

  static KinesisStreamConsumerArn fromString(String arn) {
    return ArnParser.parse(arn, KinesisStreamConsumerArn.class);
  }

}