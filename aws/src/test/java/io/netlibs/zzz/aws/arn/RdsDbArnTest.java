package io.netlibs.zzz.aws.arn;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RdsDbArnTest {

  @Test
  void testDb() {

    RdsDbArn arn = RdsDbArn.fromString("arn:aws:rds:us-west-2:12345:db:my-test-db");
    assertEquals("12345", arn.accountId());
    assertEquals("rds", arn.service());
    assertEquals("my-test-db", arn.instanceId());

  }

  @Test
  void testCluster() {

    RdsClusterArn arn = RdsClusterArn.fromString("arn:aws:rds:us-west-2:12345:cluster:my-cluster");
    assertEquals("12345", arn.accountId());
    assertEquals("rds", arn.service());
    assertEquals("my-cluster", arn.clusterId());

  }

}
