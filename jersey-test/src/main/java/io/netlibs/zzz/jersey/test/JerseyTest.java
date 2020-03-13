package io.netlibs.zzz.jersey.test;

import java.util.Set;
import java.util.function.Supplier;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.internal.ServiceFinder;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.spi.TestContainer;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import io.netlibs.zzz.jersey.JwtAuthorizationFilter;
import io.netlibs.zzz.jersey.RestConfig;
import io.netlibs.zzz.runner.Runner;

/**
 * 
 */

public abstract class JerseyTest {

  private final org.slf4j.Logger log;

  protected JerseyTest() {
    log = org.slf4j.LoggerFactory.getLogger(getClass());
  }

  static {
    Runner.prepare();
  }

  private Client client;
  private TestContainer container;
  protected Supplier<WebTarget> target;

  public abstract Set<Object> resource();

  @BeforeEach
  void setUp() {
    TestContainerFactory factory = ServiceFinder.find(TestContainerFactory.class).toArray()[0];
    RestConfig restConfig = new RestConfig();
    resource().forEach(restConfig::register);
    restConfig.registerInstances(new JwtAuthorizationFilter());
    DeploymentContext ctx = DeploymentContext.builder(restConfig).build();
    this.container = factory.create(UriBuilder.fromUri("http://localhost/").port(9998).build(), ctx);
    this.client = ClientBuilder.newClient(container.getClientConfig());
    this.target = () -> client.target(container.getBaseUri());
  }

  @AfterEach
  void tearDown() {
    this.container.stop();
    this.client.close();
    this.target = null;
  }

}
