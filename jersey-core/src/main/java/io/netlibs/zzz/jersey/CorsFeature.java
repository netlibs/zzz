package io.netlibs.zzz.jersey;

import java.lang.reflect.Method;

import javax.ws.rs.OPTIONS;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;

import io.netlibs.zzz.jersey.cors.AccessControl;

public class CorsFeature implements DynamicFeature {

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CorsFeature.class);

  @Override
  public void configure(ResourceInfo resourceInfo, FeatureContext context) {

    Method method = resourceInfo.getResourceMethod();

    Class<?> klass = method.getDeclaringClass();

    // check for impossible combinations
    if (method.isAnnotationPresent(OPTIONS.class) && method.isAnnotationPresent(AccessControl.class)) {
      log.error("Resource method {} is annotated with @AccessControl, which is not applicable for methods annotated with @OPTIONS", resourceInfo);
      return;
    }
    else if (!method.isAnnotationPresent(OPTIONS.class) && method.isAnnotationPresent(AccessControl.Preflight.class)) {
      log.error("Resource method {} is annotated with @AccessControl.Preflight, which is only applicable for methods annotated with @OPTIONS", resourceInfo);
      return;
    }

    addCorsFilter(method, klass, context);

    addCorsPreflightFilter(method, klass, context);

  }

  private void addCorsFilter(Method method, Class<?> klass, FeatureContext context) {

    if (!klass.isAnnotationPresent(AccessControl.class) && !method.isAnnotationPresent(AccessControl.class)) {
      return;
    }

    CorsResourceConfig config = getDefaultResourceConfig(context);

    if (klass.isAnnotationPresent(AccessControl.class)) {
      if (method.isAnnotationPresent(OPTIONS.class)) {
        // do not add a filter
        return;
      }
      applyCorsAnnotation(config, klass.getAnnotation(AccessControl.class));
    }

    if (method.isAnnotationPresent(AccessControl.class)) {
      applyCorsAnnotation(config, method.getAnnotation(AccessControl.class));
    }

    context.register(new CorsResponseFilter(config));

  }

  private static void applyCorsAnnotation(CorsResourceConfig config, AccessControl ann) {

    if (!ann.allowOrigin().isEmpty()) {
      config.allowOrigin = ann.allowOrigin();
    }

    if (!ann.exposeHeaders().isEmpty()) {
      config.exposeHeaders = ann.exposeHeaders();
    }

    if (ann.allowCredentials() != AccessControl.Ternary.NEUTRAL) {
      config.allowCredentials = ann.allowCredentials();
    }

  }

  private void addCorsPreflightFilter(Method method, Class<?> klass, FeatureContext context) {

    if (!klass.isAnnotationPresent(AccessControl.Preflight.class) && !method.isAnnotationPresent(AccessControl.Preflight.class)) {
      return;
    }

    CorsPreflightConfig config = getDefaultPreflightConfig(context);

    if (klass.isAnnotationPresent(AccessControl.Preflight.class)) {
      // if (!method.isAnnotationPresent(OPTIONS.class)) {
      // return;
      // }
      applyCorsPreflightAnnotation(config, klass.getAnnotation(AccessControl.Preflight.class));
    }

    if (method.isAnnotationPresent(AccessControl.Preflight.class)) {
      applyCorsPreflightAnnotation(config, method.getAnnotation(AccessControl.Preflight.class));
    }

    context.register(new CorsPreflightResponseFilter(config));

  }

  private static void applyCorsPreflightAnnotation(CorsPreflightConfig config, AccessControl.Preflight ann) {

    if (ann.maxAge() != AccessControl.Preflight.UNSET_MAX_AGE) {
      config.maxAge = ann.maxAge();
    }

    if (!ann.allowMethods().isEmpty()) {
      config.allowMethods = ann.allowMethods();
    }

    if (!ann.allowHeaders().isEmpty()) {
      config.allowHeaders = ann.allowHeaders();
    }

    if (ann.allowCredentials() != AccessControl.Ternary.NEUTRAL) {
      config.allowCredentials = ann.allowCredentials();
    }

    if (!ann.allowOrigin().isEmpty()) {
      config.allowOrigin = ann.allowOrigin();
    }

  }

  static class CorsResourceConfig {
    String allowOrigin;
    String exposeHeaders;
    AccessControl.Ternary allowCredentials;
  }

  static class CorsPreflightConfig {
    int maxAge;
    String allowMethods;
    String allowHeaders;
    AccessControl.Ternary allowCredentials;
    String allowOrigin;
  }

  private CorsResourceConfig getDefaultResourceConfig(FeatureContext context) {
    CorsResourceConfig c = new CorsResourceConfig();
    return c;
  }

  private CorsPreflightConfig getDefaultPreflightConfig(FeatureContext context) {
    CorsPreflightConfig c = new CorsPreflightConfig();
    return c;
  }

}
