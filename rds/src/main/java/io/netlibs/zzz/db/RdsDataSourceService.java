package io.netlibs.zzz.db;

import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;

public class RdsDataSourceService extends AbstractScheduledService {

  private RdsServer server;
  private HikariDataSource dataSource;
  private AwsCredentialsProvider credentialsProvider;

  public RdsDataSourceService(AwsCredentialsProvider credentialsProvider, RdsServer server, HikariConfig config) {
    this.server = server;
    this.credentialsProvider = credentialsProvider;
    this.dataSource = new HikariDataSource(config);
  }

  @Override
  protected void runOneIteration() throws Exception {
    this.dataSource.setPassword(server.dbpass(this.credentialsProvider));
  }

  @Override
  protected Scheduler scheduler() {
    return Scheduler.newFixedDelaySchedule(0, 10, TimeUnit.MINUTES);
  }

  public static RdsDataSourceService create(HikariConfig config, String rdsInstanceId, String dbuser, String dbname, String region) {
    AwsCredentialsProvider credentialsProvider = DefaultCredentialsProvider.builder().build();
    RdsServer server = RdsServer.forInstance(credentialsProvider, rdsInstanceId, dbuser, dbname, Region.of(region));
    return new RdsDataSourceService(credentialsProvider, server, config);
  }

  public static RdsDataSourceService create(String rdsInstanceId, String dbuser, String dbname, String region) {
    AwsCredentialsProvider credentialsProvider = DefaultCredentialsProvider.builder().build();
    RdsServer server = RdsServer.forInstance(credentialsProvider, rdsInstanceId, dbuser, dbname, Region.of(region));
    HikariConfig config = defaultHikariConfig();
    config.setUsername(dbuser);
    config.setPassword(server.dbpass(credentialsProvider));
    config.setJdbcUrl(server.jdbcUrl());
    return new RdsDataSourceService(credentialsProvider, server, config);
  }

  public static HikariConfig defaultHikariConfig() {
    HikariConfig config = new HikariConfig();
    config.setMinimumIdle(0);
    config.setMaximumPoolSize(2);
    config.setIdleTimeout(10000);
    config.setMaxLifetime(30000);
    config.setReadOnly(true);
    return config;
  }

  public DataSource dataSource() {
    return this.dataSource;
  }

}
