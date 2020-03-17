package io.netlibs.zzz.jersey;

import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS;
import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS;
import static com.google.common.net.HttpHeaders.ORIGIN;

import java.io.IOException;
import java.util.Arrays;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;

import com.google.common.base.Strings;

import io.netlibs.zzz.jersey.CorsFeature.CorsResourceConfig;
import io.netlibs.zzz.jersey.cors.AccessControl.Ternary;

public class CorsResponseFilter
    implements ContainerResponseFilter {

  private CorsResourceConfig config;

  public CorsResponseFilter(CorsResourceConfig config) {
    this.config = config;
  }

  @Override
  public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {

    String incomingOrigin = request.getHeaderString(ORIGIN);

    if (incomingOrigin == null) {
      return;
    }

    MultivaluedMap<String, Object> h = response.getHeaders();

    if (!Strings.isNullOrEmpty(config.allowOrigin)) {
      h.putIfAbsent(ACCESS_CONTROL_ALLOW_ORIGIN, Arrays.asList(config.allowOrigin));
    }
    else {
      h.putIfAbsent(ACCESS_CONTROL_ALLOW_ORIGIN, Arrays.asList(incomingOrigin));
    }

    if (!Strings.isNullOrEmpty(config.exposeHeaders)) {
      h.putIfAbsent(ACCESS_CONTROL_EXPOSE_HEADERS, Arrays.asList(config.exposeHeaders));
    }

    if (config.allowCredentials == Ternary.TRUE) {
      h.putIfAbsent(ACCESS_CONTROL_ALLOW_CREDENTIALS, Arrays.asList("true"));
    }

  }

}
