package io.netlibs.zzz.jersey;

import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY, isGetterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY, setterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.NONE)
@JsonPropertyOrder({ "code", "message" })
public class ErrorResponse {

	private static final String NAME = "error";

	private final int code;
	private final String message;
	private JsonNode details;

	public ErrorResponse(int code, String message) {
		this.code = code;
		this.message = message;
	}

	@JsonCreator
	public ErrorResponse(@JsonProperty(value = "code", required = true) int code,
			@JsonProperty(value = "message", required = true) String message,
			@JsonProperty(value = "details") Map<String, Object> details) {
		this(code, message);
		this.details = ObjectMapperFactory.objectMapper().convertValue(details, JsonNode.class);
	}

	@JsonCreator
	public ErrorResponse(@JsonProperty(value = "code", required = true) int code,
			@JsonProperty(value = "message", required = true) String message,
			@JsonProperty(value = "details") JsonNode details) {
		this(code, message);
		this.details = details;
	}

	@JsonProperty
	public int getCode() {
		return code;
	}

	@JsonProperty
	public String getMessage() {
		return message;
	}

	@JsonProperty
	public JsonNode getDetails() {
		return details;
	}

	@JsonIgnore
	public Map<String, ErrorResponse> wrapped() {
		return Collections.singletonMap(NAME, this);
	}
	
}