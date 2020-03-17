package io.netlibs.zzz.jersey;

import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS;
import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS;
import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS;
import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_MAX_AGE;
import static com.google.common.net.HttpHeaders.ORIGIN;

import java.io.IOException;
import java.util.Arrays;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;

import com.google.common.base.Strings;

import io.netlibs.zzz.jersey.CorsFeature.CorsPreflightConfig;
import io.netlibs.zzz.jersey.cors.AccessControl.Ternary;

public class CorsPreflightResponseFilter implements ContainerResponseFilter {

  private CorsPreflightConfig config;

  public CorsPreflightResponseFilter(CorsPreflightConfig config) {
    this.config = config;
  }

  @Override
  public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {

    String incomingOrigin = request.getHeaderString(ORIGIN);

    if (incomingOrigin == null) {
      return;
    }

    MultivaluedMap<String, Object> h = response.getHeaders();

    if (Strings.isNullOrEmpty(config.allowOrigin)) {
      h.putIfAbsent(ACCESS_CONTROL_ALLOW_ORIGIN, Arrays.asList(incomingOrigin));
    }
    else {
      h.putIfAbsent(ACCESS_CONTROL_ALLOW_ORIGIN, Arrays.asList(config.allowOrigin));
    }

    h.putIfAbsent(ACCESS_CONTROL_MAX_AGE, Arrays.asList(Integer.toString(config.maxAge)));

    if (!Strings.isNullOrEmpty(config.allowMethods)) {
      h.putIfAbsent(ACCESS_CONTROL_ALLOW_METHODS, Arrays.asList(config.allowMethods));
    }

    if (!Strings.isNullOrEmpty(config.allowHeaders)) {
      h.putIfAbsent(ACCESS_CONTROL_ALLOW_HEADERS, Arrays.asList(config.allowHeaders));
    }

    if (config.allowCredentials == Ternary.TRUE) {
      h.putIfAbsent(ACCESS_CONTROL_ALLOW_CREDENTIALS, Arrays.asList("true"));
    }

  }

}
