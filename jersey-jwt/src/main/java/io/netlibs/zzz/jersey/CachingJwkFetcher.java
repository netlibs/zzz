package io.netlibs.zzz.jersey;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.auth0.jwk.Jwk;
import com.github.benmanes.caffeine.cache.AsyncCacheLoader;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;

public class CachingJwkFetcher implements JwkFetcher, AsyncCacheLoader<JwkKeyId, Optional<Jwk>> {

  private final AsyncLoadingCache<JwkKeyId, Optional<Jwk>> cache;
  private final JwkFetcher provider;

  private CachingJwkFetcher(JwkFetcher provider) {
    this.provider = provider;
    this.cache =
      Caffeine.newBuilder()
        .maximumSize(10_000)
        .buildAsync(this);
  }

  @Override
  public CompletableFuture<Optional<Jwk>> fetch(JwkKeyId keyId) {
    return cache.get(keyId);
  }

  @Override
  public @NonNull CompletableFuture<Optional<Jwk>> asyncLoad(@NonNull JwkKeyId key, @NonNull Executor executor) {
    return provider.fetch(key);
  }

  public static JwkFetcher wrap(JwkFetcher child) {
    return new CachingJwkFetcher(child);
  }

}
