package io.netlibs.zzz.db;

import java.time.Instant;

import com.google.common.net.HostAndPort;

import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.signer.Aws4Signer;
import software.amazon.awssdk.auth.signer.params.Aws4PresignerParams;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.regions.Region;

public class Rds {

  /**
   * provides a token for RDS IAM, which is valid for connecting to the given instance.
   */

  public static String iamCredentials(AwsCredentials credentials, Instant expireAt, Region region, HostAndPort endpoint, String username) {

    Aws4PresignerParams params =
      Aws4PresignerParams.builder()
        .expirationTime(expireAt)
        .signingName("rds-db")
        .signingRegion(region)
        .awsCredentials(credentials)
        .build();

    SdkHttpFullRequest request =
      SdkHttpFullRequest
        .builder()
        .encodedPath("/")
        .host(endpoint.getHost())
        .port(endpoint.getPortOrDefault(-1))
        .protocol("http") // Will be stripped off; but we need to satisfy SdkHttpFullRequest
        .method(SdkHttpMethod.GET)
        .appendRawQueryParameter("Action", "connect")
        .appendRawQueryParameter("DBUser", username)
        .build();

    return Aws4Signer.create().presign(request, params).getUri().toString().substring("http://".length());

  }

}
