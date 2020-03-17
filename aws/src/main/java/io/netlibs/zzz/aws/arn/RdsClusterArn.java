package io.netlibs.zzz.aws.arn;

import org.immutables.value.Value;

@Value.Immutable
public interface RdsClusterArn extends ArnUri {

  String clusterId();

  static RdsClusterArn fromString(String arn) {
    return ArnParser.parse(arn, RdsClusterArn.class);
  }

}
