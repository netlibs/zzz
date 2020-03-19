package io.netlibs.zzz.db;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.google.common.net.HostAndPort;

import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.Endpoint;

public class RdsServer {

  private HostAndPort target;
  private String dbuser;
  private String dbname;
  private Region region;

  private RdsServer(Region region, HostAndPort target, String dbuser, String dbname) {
    this.region = region;
    this.target = target;
    this.dbuser = dbuser;
    this.dbname = dbname;
  }

  public static RdsServer forInstance(AwsCredentials credentials, String rdsInstanceId, String dbuser, String dbname, Region region) {
    return forInstance(StaticCredentialsProvider.create(credentials), rdsInstanceId, dbuser, dbname, region);
  }

  public static RdsServer forInstance(AwsCredentialsProvider credentialsProvider, String rdsInstanceId, String dbuser, String dbname, Region region) {

    RdsClient rds =
      RdsClient.builder()
        .credentialsProvider(credentialsProvider)
        .build();

    Endpoint endpoint =
      rds.describeDBInstances(r -> r.dbInstanceIdentifier(rdsInstanceId))
        .dbInstances()
        .stream()
        .map(e -> e.endpoint())
        .findAny()
        .orElse(null);

    String hostname = endpoint.address();
    int port = endpoint.port();

    return new RdsServer(
      region,
      HostAndPort.fromParts(hostname, port),
      dbuser,
      dbname);

  }

  public String dbpass(AwsCredentialsProvider credentialsProvider) {
    return Rds.iamCredentials(credentialsProvider.resolveCredentials(), Instant.now().plus(15, ChronoUnit.MINUTES), this.region, this.target, this.dbuser);
  }

  public String jdbcUrl() {
    return String.format("jdbc:postgresql://%s:%d/%s", target.getHost(), target.getPort(), dbname);
  }

}
