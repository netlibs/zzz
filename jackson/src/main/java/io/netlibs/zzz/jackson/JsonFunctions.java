package io.netlibs.zzz.jackson;

import java.io.IOException;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Verify;

public class JsonFunctions {

  public static <T> Function<String, T> readValue(Class<T> valueType) {
    ObjectMapper mapper = ObjectMapperFactory.objectMapper();
    JavaType jacksonType = mapper.constructType(valueType);
    Verify.verify(mapper.canDeserialize(jacksonType));
    return jsonString -> {
      try {
        return mapper.readValue(jsonString, jacksonType);
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
    };
  }

}
