package io.netlibs.zzz.jersey;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Priority;
import javax.inject.Singleton;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import com.google.common.collect.ImmutableList;
import com.google.common.net.HttpHeaders;

@Singleton
@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthorizationFilter implements ContainerRequestFilter {

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JwtAuthorizationFilter.class);
  private static final String AUTH_SCHEME = ("Bearer").toLowerCase();
  private final JwkFetcher jwkProvider;

  public JwtAuthorizationFilter() {
    OkHttpJwkFetcher fetcher = new OkHttpJwkFetcher(new JwkWellKnownResolver());
    this.jwkProvider = CachingJwkFetcher.wrap(fetcher);
  }

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {

    List<String> authorizations = requestContext.getHeaders().get(HttpHeaders.AUTHORIZATION);

    if (authorizations == null) {
      log.debug("no authoriation headers");
      return;
    }

    try {

      SecurityContext ctx = verifyContext(authorizations);

      if (ctx == null) {
        log.debug("no security context from authorization headers?");
        return;
      }

      requestContext.setSecurityContext(ctx);

    }
    catch (ExecutionException e) {
      requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
    }
    catch (TimeoutException e) {
      requestContext.abortWith(Response.status(Response.Status.SERVICE_UNAVAILABLE).build());
    }
    catch (InterruptedException e) {
      requestContext.abortWith(Response.status(Response.Status.SERVICE_UNAVAILABLE).build());
      Thread.currentThread().interrupt();
    }

  }

  private SecurityContext verifyContext(List<String> authorizations) throws InterruptedException, ExecutionException, TimeoutException {

    List<String> jwts =
      authorizations.stream()
        .map(String::trim)
        .filter(e -> e.toLowerCase().startsWith(AUTH_SCHEME + " "))
        .map(e -> (e.substring(AUTH_SCHEME.length() + 1)).trim())
        .collect(ImmutableList.toImmutableList());

    if (jwts.isEmpty()) {
      log.info("Authorization headers without Bearers");
      return null;
    }

    return JwtSecurtyContext.fromTokens(jwkProvider, jwts).get(5, TimeUnit.SECONDS);

  }

}
