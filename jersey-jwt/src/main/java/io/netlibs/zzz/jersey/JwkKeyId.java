package io.netlibs.zzz.jersey;

import java.net.URI;

import org.immutables.value.Value;

import io.netlibs.zzz.jersey.ImmutableJwkKeyId;

/**
 * a key id combined with an authority that provided it. ensures we don't mix up the source of a key
 * when retrieving from multiple locations.
 * 
 * @author theo
 *
 */

@Value.Immutable
public interface JwkKeyId {

  @Value.Parameter
  URI authority();

  @Value.Parameter
  String keyId();

  static JwkKeyId of(URI authority, String keyId) {
    return ImmutableJwkKeyId.of(authority, keyId);
  }

  static JwkKeyId of(String uri, String keyId) {
    return of(URI.create(uri), keyId);
  }

}
