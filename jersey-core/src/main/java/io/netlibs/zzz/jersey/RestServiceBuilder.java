package io.netlibs.zzz.jersey;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class RestServiceBuilder {

  private int port = 0;
  private MeterRegistry meterRegistry = new SimpleMeterRegistry();
  private RestConfig restConfig;

  public RestServiceBuilder port(int port) {
    this.port = port;
    return this;
  }

  public RestServiceBuilder meterRegistry(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
    return this;
  }

  public RestServiceBuilder register(Object component) {
    this.restConfig.register(component);
    return this;
  }

  public RestServer build() {
    return new RestServer(restConfig, meterRegistry, port);
  }

}
