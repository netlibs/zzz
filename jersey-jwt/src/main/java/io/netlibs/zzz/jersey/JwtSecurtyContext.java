package io.netlibs.zzz.jersey;

import java.security.Principal;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
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
import com.google.common.collect.ImmutableList;

public class JwtSecurtyContext implements SecurityContext, Principal {

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JwtSecurtyContext.class);
  private ImmutableList<DecodedJWT> jwts;

  public JwtSecurtyContext(ImmutableList<DecodedJWT> jwts) {
    this.jwts = jwts;
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

  public static CompletableFuture<SecurityContext> fromTokens(JwkFetcher jwkProvider, List<String> jwts) {
    JwtSecurtyContext res =
      new JwtSecurtyContext(
        jwts.stream()
          .map(token -> verify(jwkProvider, token))
          .collect(ImmutableList.toImmutableList()));
    return CompletableFuture.completedFuture(res);
  }

  private static DecodedJWT verify(JwkFetcher jwkProvider, String token) {

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

    return verifier.verify(token);

  }

  @Override
  public String getName() {
    return this.jwts.get(0).getSubject();
  }

  @Override
  public String toString() {
    return this.jwts.get(0).getSubject();
  }

}
