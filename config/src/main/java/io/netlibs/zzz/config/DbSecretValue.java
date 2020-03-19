package io.netlibs.zzz.config;

import java.net.URI;
import java.net.URISyntaxException;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Value.Immutable
@Value.Style
@JsonDeserialize(builder = ImmutableDbSecretValue.Builder.class)
public interface DbSecretValue extends WithDbSecretValue {

  String engine();

  String username();

  @Value.Redacted
  String password();

  String host();

  int port();

  String dbname();

  default URI toJdbcUrl() {
    String scheme = String.format("jdbc:%s", engine());
    String userinfo = String.format("%s:%s", username(), password());
    try {
      return new URI(scheme, userinfo, host(), port(), "/" + dbname(), null, null);
    }
    catch (URISyntaxException e) {
      // TODO Auto-generated catch block
      throw new RuntimeException(e);
    }
  }

}
