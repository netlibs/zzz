package io.netlibs.zzz.db;

import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.zaxxer.hikari.HikariDataSource;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

public class RdsCredentialService extends AbstractScheduledService {

  private HikariDataSource dataSource;
  private RdsServer server;
  private AwsCredentialsProvider credentialsProvider;

  public RdsCredentialService(AwsCredentialsProvider credentialsProvider, HikariDataSource dataSource, RdsServer server) {
    this.credentialsProvider = credentialsProvider;
    this.dataSource = dataSource;
    this.server = server;
  }

  @Override
  protected void runOneIteration() throws Exception {
    this.dataSource.setPassword(this.server.dbpass(credentialsProvider));
  }

  @Override
  protected Scheduler scheduler() {
    return Scheduler.newFixedDelaySchedule(10, 10, TimeUnit.MINUTES);
  }

}
