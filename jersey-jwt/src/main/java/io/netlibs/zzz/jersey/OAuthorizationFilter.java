package io.netlibs.zzz.jersey;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

@Secured
@Provider
@Priority(Priorities.AUTHORIZATION)
public class OAuthorizationFilter implements ContainerRequestFilter {

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OAuthorizationFilter.class);

  @Context
  private ResourceInfo resourceInfo;

  /**
   * 
   * @param annotatedElement
   * @return
   */

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {

    Class<?> resourceClass = resourceInfo.getResourceClass();
    Package resourcePackage = resourceClass.getPackage();
    Method resourceMethod = resourceInfo.getResourceMethod();

    List<Secured> items =
      Stream.of(extract(resourceClass), extract(resourcePackage), extract(resourceMethod))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());

    ImmutableSet<String> allowedIssuers =
      items.stream()
        .flatMap(ant -> Arrays.stream(ant.issuer()))
        .collect(ImmutableSet.toImmutableSet());

    ImmutableSet<String> requiredAudiences =
      items.stream()
        .flatMap(ant -> Arrays.stream(ant.audience()))
        .collect(ImmutableSet.toImmutableSet());

    ImmutableSet<String> scopes =
      items.stream()
        .flatMap(ant -> Arrays.stream(ant.scopes()))
        .collect(ImmutableSet.toImmutableSet());

    try {

      JwtSecurtyContext jwt = (JwtSecurtyContext) requestContext.getSecurityContext();

      checkIssuer(jwt, allowedIssuers);
      checkAudiences(jwt, requiredAudiences);
      checkPermissions(jwt, scopes);
      
      // all passed!

    }
    catch (WebApplicationException e) {

      requestContext.abortWith(e.getResponse());

    }
    catch (Exception e) {

      log.warn("JWT problem: {}", e.getMessage(), e);

      requestContext.abortWith(
        Response.status(
          Response.Status.FORBIDDEN)
          .build());

    }

  }

  /**
   * 
   * @param annotatedElement
   * @return
   */

  private Optional<Secured> extract(AnnotatedElement annotatedElement) {

    if (annotatedElement == null) {
      return Optional.empty();
    }

    Secured secured = annotatedElement.getAnnotation(Secured.class);

    if (secured == null) {
      return Optional.empty();
    }

    return Optional.of(secured);
  }

  private void checkIssuer(JwtSecurtyContext jwt, ImmutableSet<String> allowedIssuers) {

    if (allowedIssuers.isEmpty()) {
      return;
    }

    if (allowedIssuers.contains(jwt.issuer())) {
      return;
    }

    log.warn("JWT had unsupported issuer {}", jwt.issuer());

    ObjectNode body = JsonNodeFactory.instance.objectNode();

    ObjectNode error = body.putObject("error");

    error.put("code", "unsupported_issuer");

    throw new WebApplicationException(
      Response.status(Status.FORBIDDEN)
        .type(MediaType.APPLICATION_JSON)
        .entity(body)
        .build());

  }

  private void checkAudiences(JwtSecurtyContext ctx, Set<String> requiredAudiences) {

    if (requiredAudiences.isEmpty()) {
      return;
    }

    SetView<String> matches = Sets.intersection(requiredAudiences, ctx.audiences());

    if (!matches.isEmpty()) {
      log.info("aud req {}, have {}", ctx.audiences(), requiredAudiences);
      return;
    }

    log.warn("JWT had missing audience {} (got {})", requiredAudiences, ctx.audiences());

    ObjectNode body = JsonNodeFactory.instance.objectNode();

    ObjectNode error = body.putObject("error");

    error.put("code", "missing_audience");

    throw new WebApplicationException(
      Response.status(Status.FORBIDDEN)
        .type(MediaType.APPLICATION_JSON)
        .entity(body)
        .build());

  }

  private void checkPermissions(JwtSecurtyContext ctx, ImmutableSet<String> required) {

    if (required.isEmpty()) {
      return;
    }

    SetView<String> contains = Sets.intersection(required, ctx.scopes());

    if (!contains.isEmpty()) {
      log.info("have scopes we need: {}", contains);
      return;
    }

    log.warn(
      "attempted to access resource requiring one of {}, only had scopes {}",
      required,
      ctx.scopes());

    ObjectNode body = JsonNodeFactory.instance.objectNode();

    ObjectNode error = body.putObject("error");

    error.put("code", "insufficient_scope");
    error.put("require", required.stream().collect(Collectors.joining(", ")));

    throw new WebApplicationException(
      Response.status(Status.FORBIDDEN)
        .type(MediaType.APPLICATION_JSON)
        .entity(body)
        .build());

  }

}
