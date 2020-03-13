package io.netlibs.zzz.jersey;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException>, ErrorableResponse {

	@Override
	public Response toResponse(RuntimeException exception) {
		exception.printStackTrace();
		return buildResponse(Response.Status.INTERNAL_SERVER_ERROR, exception.getMessage());
	}

}