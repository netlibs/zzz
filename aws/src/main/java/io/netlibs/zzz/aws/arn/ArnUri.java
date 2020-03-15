package io.netlibs.zzz.aws.arn;

import com.google.common.base.Joiner;

// arn:partition:service:region:account-id:resource
// arn:partition:service:region:account-id:resourcetype/resource
// arn:partition:service:region:account-id:resourcetype/resource/qualifier
// arn:partition:service:region:account-id:resourcetype/resource:qualifier
// arn:partition:service:region:account-id:resourcetype:resource
// arn:partition:service:region:account-id:resourcetype:resource:qualifier

public interface ArnUri {

  String partition();

  String service();

  String region();

  String accountId();

  String resource();

  default String toArn() {
    return Joiner.on(':').join("arn", partition(), service(), region(), accountId(), resource());
  }

}