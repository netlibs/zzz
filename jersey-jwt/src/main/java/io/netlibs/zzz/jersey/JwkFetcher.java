package io.netlibs.zzz.jersey;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.auth0.jwk.Jwk;

public interface JwkFetcher {

	CompletableFuture<Optional<Jwk>> fetch(JwkKeyId keyId);

}
