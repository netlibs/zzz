package io.netlibs.zzz.jersey;

import java.util.Optional;

public interface JsonWebToken {

  <T> Optional<T> claim(String claimName, Class<T> type);

}
