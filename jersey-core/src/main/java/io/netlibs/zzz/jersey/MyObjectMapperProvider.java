package io.netlibs.zzz.jersey;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.netlibs.zzz.jackson.ObjectMapperFactory;


@Provider
public class MyObjectMapperProvider implements ContextResolver<ObjectMapper> {

  final ObjectMapper defaultObjectMapper;

  public MyObjectMapperProvider() {
    defaultObjectMapper = ObjectMapperFactory.objectMapper();
  }

  @Override
  public ObjectMapper getContext(final Class<?> type) {
    return defaultObjectMapper;
  }

}