package io.netlibs.zzz.jersey;

public class RestServiceBuilder {

  private int port = 0;
  private RestConfig restConfig = new RestConfig();

  public RestServiceBuilder port(int port) {
    this.port = port;
    return this;
  }


  public RestServiceBuilder register(Object component) {
    this.restConfig.register(component);
    return this;
  }

  public RestServer build() {
    return new RestServer(restConfig, port);
  }

}
