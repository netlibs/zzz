package fluentcloud.db;

import javax.sql.DataSource;

import com.google.common.util.concurrent.AbstractService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;

public class RdsDataSourceService extends AbstractService {

  private RdsServer server;
  private HikariConfig config;
  private AwsCredentialsProvider credentialsProvider;
  private HikariDataSource dataSource;
  private RdsCredentialService credentialService;

  public RdsDataSourceService(AwsCredentialsProvider credentialsProvider, RdsServer server, HikariConfig config) {
    this.server = server;
    this.config = config;
    this.credentialsProvider = credentialsProvider;
  }

  @Override
  protected void doStart() {
    this.dataSource = new HikariDataSource(config);
    this.credentialService = new RdsCredentialService(dataSource, server);
    this.credentialService.startAsync().awaitRunning();
    this.notifyStarted();
  }

  @Override
  protected void doStop() {
    this.credentialService.stopAsync().awaitTerminated();
    this.notifyStopped();
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
    config.setPassword(server.dbpass());
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
