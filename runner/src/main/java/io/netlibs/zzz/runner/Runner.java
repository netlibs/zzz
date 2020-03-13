package io.netlibs.zzz.runner;

import org.slf4j.bridge.SLF4JBridgeHandler;

public class Runner {

  public static void prepare() {
    java.security.Security.setProperty("networkaddress.cache.ttl", "15");
    if (!SLF4JBridgeHandler.isInstalled()) {
      SLF4JBridgeHandler.removeHandlersForRootLogger();
      SLF4JBridgeHandler.install();
    }
  }

}
