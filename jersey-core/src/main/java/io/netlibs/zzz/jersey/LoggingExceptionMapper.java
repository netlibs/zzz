package io.netlibs.zzz.jersey;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class LoggingExceptionMapper implements ExceptionMapper<Throwable> {

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoggingExceptionMapper.class);

  @Override
  public Response toResponse(Throwable exception) {

    // if it's a WebApplicationException then we can use directly. just log, too.
    if (exception instanceof WebApplicationException) {
      return processWebApplicationException((WebApplicationException) exception);
    }

    log.error("unexpected error: {}", exception.getMessage(), exception);

    return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .type(MediaType.APPLICATION_JSON_TYPE)
      .entity(new ErrorResponse("internal error"))
      .build();
  }

  private Response processWebApplicationException(WebApplicationException exception) {

    final Response response = exception.getResponse();

    Response.Status.Family family = response.getStatusInfo().getFamily();

    if (family.equals(Response.Status.Family.REDIRECTION)) {
      return response;
    }

    if (family.equals(Response.Status.Family.SERVER_ERROR)) {
      log.error("ServerError: {}", exception.getMessage(), exception);
    }

    return Response.fromResponse(response)
      .type(MediaType.APPLICATION_JSON_TYPE)
      .entity(new ErrorResponse(response.getStatus(), exception.getLocalizedMessage()))
      .build();

  }

}
