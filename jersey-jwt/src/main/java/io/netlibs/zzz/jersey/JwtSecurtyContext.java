package io.netlibs.zzz.jersey;

import java.io.IOException;
import java.security.Principal;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.core.SecurityContext;

import com.auth0.jwk.InvalidPublicKeyException;
import com.auth0.jwk.Jwk;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

public class JwtSecurtyContext implements SecurityContext, Principal, JsonWebToken {

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JwtSecurtyContext.class);
  private ObjectNode jwts;
  private ObjectMapper mapper;

  public JwtSecurtyContext(ObjectMapper mapper, ImmutableList<ObjectNode> jwts) {
    this.jwts = jwts.get(0);
    this.mapper = mapper;
  }

  @Override
  public Principal getUserPrincipal() {
    return this;
  }

  @Override
  public boolean isUserInRole(String role) {
    log.info("rejecting request for role '{}'", role);
    return false;
  }

  @Override
  public boolean isSecure() {
    // note that we always transmit over secure channel. no frontends allow plain http
    return true;
  }

  @Override
  public String getAuthenticationScheme() {
    return "bearer";
  }

  public static CompletableFuture<SecurityContext> fromTokens(JwkFetcher jwkProvider, List<String> jwts, ObjectMapper mapper) {
    JwtSecurtyContext res =
      new JwtSecurtyContext(
        mapper,
        jwts.stream()
          .map(token -> verify(jwkProvider, token, mapper))
          .collect(ImmutableList.toImmutableList()));
    return CompletableFuture.completedFuture(res);
  }

  private static ObjectNode verify(JwkFetcher jwkProvider, String token, ObjectMapper mapper) {

    DecodedJWT jwt = JWT.decode(token);

    log.debug("Decoded JWT: iss={}, subject={}, aud={}", jwt.getIssuer(), jwt.getSubject(), jwt.getAudience());

    JwkKeyId keyId = JwkKeyId.of(jwt.getIssuer(), jwt.getKeyId());

    RSAKeyProvider keyProvider = new RSAKeyProvider() {

      @Override
      public RSAPublicKey getPublicKeyById(String kid) {

        try {
          // Received 'kid' value might be null if it wasn't defined in the Token's header
          Jwk jwk = jwkProvider.fetch(JwkKeyId.of(keyId.authority(), kid)).get().orElseThrow();
          log.debug("JWK for JWT is: {}", jwk);
          RSAPublicKey publicKey = (RSAPublicKey) jwk.getPublicKey();
          return (RSAPublicKey) publicKey;
        }
        catch (InterruptedException | ExecutionException e) {
          // TODO Auto-generated catch block
          throw new RuntimeException(e);
        }
        catch (InvalidPublicKeyException e) {
          // TODO Auto-generated catch block
          throw new RuntimeException(e);
        }
      }

      @Override
      public RSAPrivateKey getPrivateKey() {
        return null;
      }

      @Override
      public String getPrivateKeyId() {
        return null;
      }

    };

    Algorithm alg = Algorithm.RSA256(keyProvider);

    // reusable verifier
    JWTVerifier verifier = JWT.require(alg).build();

    // throws if not validated
    DecodedJWT validated = verifier.verify(token);

    byte[] rawjson = Base64.getDecoder().decode(validated.getPayload());

    try {
      return mapper.readValue(rawjson, ObjectNode.class);
    }
    catch (IOException e) {
      // TODO Auto-generated catch block
      throw new RuntimeException(e);
    }

  }

  @Override
  public String getName() {
    return this.jwts.get("sub").textValue();
  }

  @Override
  public String toString() {
    return this.jwts.toString();
  }

  @Override
  public <T> Optional<T> claim(String claimName, Class<T> valueType) {
    return Optional.ofNullable(this.jwts.get(claimName))
      .filter(n -> !n.isMissingNode())
      .map(value -> mapper.convertValue(value, valueType));
  }

  public Range<Instant> validity() {
    return Range.closedOpen(issuedAt(), expiresAt());
  }

  public Instant issuedAt() {
    return claim("iat", Long.class).map(Instant::ofEpochSecond).orElseThrow();
  }

  public Instant expiresAt() {
    return claim("exp", Long.class).map(Instant::ofEpochSecond).orElseThrow();
  }

  public Set<String> scopes() {
    return claim("scope", String.class)
      .map(val -> ImmutableSet.copyOf(Splitter.on(" ").trimResults().omitEmptyStrings().splitToList(val)))
      .orElse(ImmutableSet.<String>of());
  }

  public String authorizingParty() {
    return claim("azp", String.class).orElse("");
  }

  public String subject() {
    return claim("sub", String.class).orElse("");
  }

  public ImmutableSet<String> audiences() {
    return claim("aud", String[].class)
      .map(ImmutableSet::copyOf)
      .orElse(ImmutableSet.of());
  }

  public String issuer() {
    return claim("iss", String.class).orElse("");
  }

  /// ----

  public Optional<String> grantType() {
    return claim("gty", String.class);
  }

  public ImmutableSet<String> permissions() {
    return ImmutableSet.copyOf(claim("permissions", String[].class).orElse(new String[0]));
  }

}
