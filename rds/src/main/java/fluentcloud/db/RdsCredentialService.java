package fluentcloud.db;

import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.zaxxer.hikari.HikariDataSource;

public class RdsCredentialService extends AbstractScheduledService {

  private HikariDataSource dataSource;
  private RdsServer server;

  public RdsCredentialService(HikariDataSource dataSource, RdsServer server) {
    this.dataSource = dataSource;
    this.server = server;
  }

  @Override
  protected void runOneIteration() throws Exception {
    this.dataSource.setPassword(this.server.dbpass());
  }

  @Override
  protected Scheduler scheduler() {
    return Scheduler.newFixedDelaySchedule(10, 10, TimeUnit.MINUTES);
  }

}
