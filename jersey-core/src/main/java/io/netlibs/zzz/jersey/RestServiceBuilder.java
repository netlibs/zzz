package io.netlibs.zzz.jersey;

import io.micrometer.core.instrument.MeterRegistry;

public class RestServiceBuilder {

  private int port;
  private MeterRegistry meterRegistry;
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
