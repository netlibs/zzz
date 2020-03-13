package io.netlibs.zzz.jersey;

import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public interface ErrorableResponse {

	/**
	 * Builds an error response from status and message.
	 *
	 * @param status  Response status
	 * @param message Error message
	 * @return response
	 */
	default Response buildResponse(Response.Status status, String message) {
		return buildResponse(status, message, null);
	}

	/**
	 * Builds an error response from status, message with details
	 *
	 * @param status  Response status
	 * @param message Error message
	 * @param details Details as Map
	 * @return response
	 */
	default Response buildResponse(Response.Status status, String message, Map<String, Object> details) {
		return Response.status(status).type(MediaType.APPLICATION_JSON_TYPE)
				.entity(new ErrorResponse(status.getStatusCode(), message, details).wrapped()).build();
	}

}