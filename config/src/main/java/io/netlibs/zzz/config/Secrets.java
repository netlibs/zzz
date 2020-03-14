package io.netlibs.zzz.config;

import org.reactivestreams.Publisher;

import io.netlibs.zzz.jackson.JsonFunctions;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClient;

public class Secrets {

  private static SecretsManagerAsyncClient secretsManager =
    SecretsManagerAsyncClient.builder()
      .build();

  public static Flowable<ImmutableSecretValue> forKey(String secretName) {
    return Single
      .fromCompletionStage(secretsManager.getSecretValue(b -> b.secretId(secretName)))
      .toFlowable()
      .concatWith(Flowable.never())
      .map(SecretValue::valueOf);
  }

  public static <T> Publisher<T> watchSecretKey(String secretKey, Class<T> klass) {
    return forKey(secretKey).map(SecretValue::secretString).map(JsonFunctions.readValue(klass)::apply);
  }

}
