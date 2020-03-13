package io.netlibs.zzz.jersey;

import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.ws.rs.core.Feature;

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.sse.SseFeature;
import org.glassfish.jersey.message.DeflateEncoder;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.EncodingFilter;

import com.google.inject.Injector;

import io.micrometer.jersey2.server.MetricsApplicationEventListener;

public class RestConfig extends ResourceConfig {

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RestConfig.class);

  public RestConfig() {

    property(CommonProperties.FEATURE_AUTO_DISCOVERY_DISABLE, true);

    this.register(DeflateEncoder.class);
    this.register(GZipEncoder.class);
    this.register(EncodingFilter.class);

    this.register(RuntimeExceptionMapper.class);
    this.register(SseFeature.class);
    this.register(CORSResponseFilter.class);
    this.register(new MyObjectMapperProvider());
    this.register(JacksonFeature.class);

  }

  @Inject
  public RestConfig(
      Set<Feature> features,
      Set<Class<? extends Feature>> featureClasses,
      MetricsApplicationEventListener listener,
      Injector injector) {

    this();

    Stream.concat(features.stream(), featureClasses.stream().map(injector::getInstance))
      .peek(f -> log.debug("registered feature: {}", f))
      .forEach(this::register);

  }

}
