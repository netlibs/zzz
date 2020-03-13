package io.netlibs.zzz.jersey;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.auth0.jwk.Jwk;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.MediaType;

import okhttp3.Response;

class WellKnownJwkTransformer {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@JsonIgnoreProperties(ignoreUnknown = false)
	public static final class Key {
		public String kid;
		public String kty;
		public String alg;
		public String use;
		public List<String> key_ops;
		public String x5u;
		public List<String> x5c;
		public String x5t;

		@JsonAnySetter
		public Map<String, Object> additionalAttributes = new HashMap<>();

		public Jwk asJwk() {
			return new Jwk(kid, kty, alg, use, key_ops, x5u, x5c, x5t, additionalAttributes);
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static final class KeyResponse {

		@JsonProperty("keys")
		public List<Key> keys = new ArrayList<>();

		Jwk load(String keyId) {
			return keys.stream().filter(e -> keyId.contentEquals(e.kid)).findAny().map(e -> toJwk(e)).orElse(null);
		}

		@JsonIgnore
		private Jwk toJwk(Key e) {
			return e.asJwk();
		}

	}

	KeyResponse transform(String json) {
		return transform(json.getBytes(StandardCharsets.UTF_8));
	}

	KeyResponse transform(byte[] json) {

		try {
			return objectMapper.readValue(json, KeyResponse.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	private KeyResponse transform(Response res) {

		if (res.code() != 200) {
			throw new IllegalArgumentException("invalid status fetching " + res.request().url() + ": " + res.code());
		}

		MediaType contentType = Optional.ofNullable(res.headers().get("content-type")).map(MediaType::parse)
				.orElse(MediaType.JSON_UTF_8);

		if (!contentType.is(MediaType.JSON_UTF_8.withoutParameters())) {
			throw new IllegalArgumentException(
					"Unexpected JWKS content type at " + res.request().url() + ": " + contentType);
		}

		try {
			return objectMapper.readValue(res.body().bytes(), KeyResponse.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public Jwk transform(Response response, JwkKeyId keyId) {
		return this.transform(response).load(keyId.keyId());
	}

}
