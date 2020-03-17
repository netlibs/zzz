package io.netlibs.zzz.aws.arn;

import org.immutables.value.Value;

@Value.Immutable
public interface RdsDbArn extends ArnUri {

  String instanceId();

  static RdsDbArn fromString(String arn) {
    return ArnParser.parse(arn, RdsDbArn.class);
  }

}
