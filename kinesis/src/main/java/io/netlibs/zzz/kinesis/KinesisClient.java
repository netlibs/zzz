package io.netlibs.zzz.kinesis;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Optional;
import java.util.OptionalInt;

import org.immutables.value.Value;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.kinesis.producer.KinesisProducer;
import com.amazonaws.services.kinesis.producer.KinesisProducerConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.netlibs.zzz.aws.arn.ArnParser;
import io.netlibs.zzz.aws.arn.KinesisStreamArn;
import io.netlibs.zzz.config.ParameterValue;
import io.netlibs.zzz.config.Parameters;
import io.netlibs.zzz.jackson.ObjectMapperFactory;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.BehaviorProcessor;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;

public class KinesisClient {

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(KinesisClient.class);

  private KinesisProducer producer;
  private BehaviorProcessor<Runnable> queue = BehaviorProcessor.create();

  private AwsCredentialsProvider credentialsProvider;
  private KinesisStreamArn streamArn;

  @Value.Immutable
  @JsonDeserialize(builder = ImmutableKinesisAggregationConfig.Builder.class)
  interface KinesisAggregationConfig {

    Optional<Boolean> enabled();

    OptionalInt maxCount();

    OptionalInt maxSize();

  }

  @Value.Immutable
  @JsonDeserialize(builder = ImmutableKinesisConfig.Builder.class)
  interface KinesisConfig {

    String streamArn();

    Optional<KinesisAggregationConfig> aggregation();

    OptionalInt rateLimit();

    OptionalInt maxConnections();

    Optional<Duration> recordTtl();

    OptionalInt threadPoolSize();

    Optional<Duration> recordMaxBufferedTime();

    Optional<Duration> requestTimeout();

  }

  private KinesisClient(AwsCredentialsProvider credentialsProvider, Flowable<ParameterValue<String>> param) {
    this.credentialsProvider = credentialsProvider;
    param.doOnNext(e -> log.info(e.toString())).map(p -> p.value()).subscribe(this::applyConfig, this::handlePermanentFailure, this::onComplete);
  }

  private void handlePermanentFailure(Throwable t) {
    log.error("kinesis configuration error: {}", t.getMessage(), t);
  }

  private void onComplete() {
    log.info("Kinesis producer shutdown");
  }

  private void applyConfig(String settings) {

    try {

      applyConfig(ObjectMapperFactory.objectMapper().readValue(settings, KinesisConfig.class));

    }
    catch (JsonProcessingException e1) {
      // TODO Auto-generated catch block
      throw new RuntimeException(e1);
    }

  }

  private void applyConfig(KinesisConfig settings) {

    log.info("Config: {}", settings);

    this.streamArn = ArnParser.parse(settings.streamArn(), KinesisStreamArn.class);

    log.info("Kinesis ARN is {}", this.streamArn);

    final KinesisProducerConfiguration config = new KinesisProducerConfiguration();

    settings.aggregation()
      .ifPresentOrElse(
        aggr -> {
          config.setAggregationEnabled(aggr.enabled().orElse(true));
          config.setAggregationMaxCount(aggr.maxCount().orElse(1024));
          config.setAggregationMaxSize(aggr.maxSize().orElse(1024 * 16));
        },
        () -> {
          config.setAggregationEnabled(true);
          config.setAggregationMaxCount(1024);
          config.setAggregationMaxSize(10224 * 16);
        });

    config.setRateLimit(settings.rateLimit().orElse(150));
    config.setMaxConnections(settings.maxConnections().orElse(24));

    config.setRecordTtl(settings.recordTtl().orElse(Duration.ofSeconds(30)).toMillis());
    config.setThreadPoolSize(settings.threadPoolSize().orElse(1));
    config.setThreadingModel(KinesisProducerConfiguration.ThreadingModel.POOLED);
    config.setRecordMaxBufferedTime(settings.recordMaxBufferedTime().orElse(Duration.ofMillis(50)).toMillis());
    config.setRequestTimeout(settings.requestTimeout().orElse(Duration.ofSeconds(6)).toMillis());

    // region based on the stream ARN.
    config.setRegion(this.streamArn.region());

    // todo: use AssumeRole if different account?
    config.setCredentialsProvider(new AWSCredentialsProvider() {

      @Override
      public void refresh() {
      }

      @Override
      public AWSCredentials getCredentials() {
        AwsSessionCredentials creds = (AwsSessionCredentials) credentialsProvider.resolveCredentials();
        return new BasicSessionCredentials(creds.accessKeyId(), creds.secretAccessKey(), creds.sessionToken());
      }

    });
    //
    this.producer = new KinesisProducer(config);

    // now we can run
    this.queue.subscribe(e -> e.run());

  }

  public void add(String partitionKey, ByteBuffer data) {
    queue.onNext(() -> this.producer.addUserRecord(this.streamArn.streamName(), partitionKey, data));
  }

  public void flushSync() {
    this.producer.flushSync();
  }

  public static KinesisClient fromParameterPath(String parameterPath) {
    return fromParameterPath(DefaultCredentialsProvider.create(), parameterPath);
  }

  static KinesisClient fromParameterPath(AwsCredentialsProvider credentialsProvider, String parameterPath) {

    Flowable<ParameterValue<String>> param =
      Parameters
        .forName(parameterPath)
        .map(e -> e
          .cast(ParameterValue.StringValue.class)
          .convert(ParameterValue.StringValue::value));

    return new KinesisClient(credentialsProvider, param);
  }

}
