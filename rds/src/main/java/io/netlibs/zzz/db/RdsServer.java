package io.netlibs.zzz.db;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.google.common.net.HostAndPort;

import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.signer.Aws4Signer;
import software.amazon.awssdk.auth.signer.params.Aws4PresignerParams;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.Endpoint;

public class RdsServer {

  private HostAndPort target;
  private String dbuser;
  private String dbpass;
  private String dbname;

  private RdsServer(HostAndPort target, String dbuser, String dbpass, String dbname) {
    this.target = target;
    this.dbuser = dbuser;
    this.dbpass = dbpass;
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
      HostAndPort.fromParts(hostname, port),
      dbuser,
      generateAuthToken(region, hostname, port, dbuser, credentialsProvider),
      dbname);

  }

  static String generateAuthToken(Region region, String hostname, int port, String username, AwsCredentialsProvider credentialsProvider) {

    Aws4PresignerParams params =
      Aws4PresignerParams.builder()
        .expirationTime(Instant.now().plus(15, ChronoUnit.MINUTES))
        .signingName("rds-db")
        .signingRegion(region)
        .awsCredentials(credentialsProvider.resolveCredentials())
        .build();

    SdkHttpFullRequest request =
      SdkHttpFullRequest
        .builder()
        .encodedPath("/")
        .host(hostname)
        .port(port)
        .protocol("http") // Will be stripped off; but we need to satisfy SdkHttpFullRequest
        .method(SdkHttpMethod.GET)
        .appendRawQueryParameter("Action", "connect")
        .appendRawQueryParameter("DBUser", username)
        .build();

    return Aws4Signer.create().presign(request, params).getUri().toString().substring("http://".length());

  }

  public String dbpass() {
    return this.dbpass;
  }

  public String jdbcUrl() {
    return String.format("jdbc:postgresql://%s:%d/%s", target.getHost(), target.getPort(), dbname);
  }

}
