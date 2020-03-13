package io.netlibs.zzz.jersey;

import java.net.URI;

import javax.inject.Inject;
import javax.inject.Named;
import javax.net.ssl.SSLContext;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import com.google.common.util.concurrent.AbstractService;

import io.micrometer.core.instrument.MeterRegistry;

public class RestServer extends AbstractService {

  private RestConfig resourceConfig;
  private HttpServer grizzlyServer;
  private MeterRegistry meterRegistry;

  private URI baseUri;

  // Default pool config values
  private int queueLimit = 64;
  private int corePoolSize = 64;
  private int maxPoolSize = 64;

  private static final String GRIZZLY_QUEUE_LIMIT_ENV = "GRIZZLY_QUEUE_LIMIT";
  private static final String GRIZZLY_CORE_POOL_SIZE_ENV = "GRIZZLY_CORE_POOL_SIZE";
  private static final String GRIZZLY_MAX_POOL_SIZE_ENV = "GRIZZLY_MAX_POOL_SIZE";

  private static final String KEYSTORE_PATH_ENV = "KEYSTORE_PATH";
  private static final String KEYSTORE_PWD_ENV = "KEYSTORE_PWD";

  @Inject
  public RestServer(RestConfig config, MeterRegistry meterRegistry, @Named("port") int port) {
    this.resourceConfig = config;
    this.meterRegistry = meterRegistry;
    this.baseUri = URI.create("http://0.0.0.0:" + port + "/");
  }

  @Override
  protected void doStart() {
    try {
      this.resolveGrizzlyConfig();
      String ff = System.getenv(KEYSTORE_PATH_ENV);

      if (ff != null) {

        SSLContextConfigurator sslContextConfig = new SSLContextConfigurator();
        sslContextConfig.setKeyStoreFile(ff);
        sslContextConfig.setKeyStorePass(System.getenv(KEYSTORE_PWD_ENV));

        SSLContext sslContext = sslContextConfig.createSSLContext(true);
        SSLEngineConfigurator sslcfg =
          new SSLEngineConfigurator(sslContext)
            .setClientMode(false)
            .setNeedClientAuth(false);

        this.grizzlyServer =
          GrizzlyHttpServerFactory.createHttpServer(
            baseUri,
            resourceConfig,
            true,
            sslcfg);
      }
      else {

        this.grizzlyServer =
          GrizzlyHttpServerFactory.createHttpServer(
            baseUri,
            resourceConfig,
            true);

      }
      for (NetworkListener l : this.grizzlyServer.getListeners()) {

        TCPNIOTransport tport = l.getTransport();

        ThreadPoolConfig wpc = tport.getWorkerThreadPoolConfig();
        wpc.setCorePoolSize(this.corePoolSize);
        wpc.setMaxPoolSize(this.maxPoolSize);
        wpc.setQueueLimit(this.queueLimit);

        ThreadPoolConfig ktp = tport.getKernelThreadPoolConfig();
        ktp.setCorePoolSize(this.corePoolSize);
        ktp.setMaxPoolSize(this.maxPoolSize);
        ktp.setQueueLimit(this.queueLimit);

      }

      this.grizzlyServer.start();
      this.notifyStarted();

    }
    catch (Exception ex) {
      ex.printStackTrace();
      this.notifyFailed(ex);
    }
  }

  @Override
  protected void doStop() {
    this.grizzlyServer.shutdownNow();
    this.notifyStopped();
  }

  private void resolveGrizzlyConfig() {
    String queueLimitConfig = System.getenv(GRIZZLY_QUEUE_LIMIT_ENV);
    String corePoolSizeConfig = System.getenv(GRIZZLY_CORE_POOL_SIZE_ENV);
    String maxPoolSizeConfig = System.getenv(GRIZZLY_MAX_POOL_SIZE_ENV);
    this.queueLimit =
      queueLimitConfig == null ? queueLimit
                               : Integer.parseInt(queueLimitConfig);
    this.corePoolSize =
      corePoolSizeConfig == null ? corePoolSize
                                 : Integer.parseInt(corePoolSizeConfig);
    this.maxPoolSize =
      maxPoolSizeConfig == null ? maxPoolSize
                                : Integer.parseInt(maxPoolSizeConfig);
  }

}
