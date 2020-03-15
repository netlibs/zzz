package io.netlibs.zzz.config;

import java.util.function.Function;

import io.netlibs.zzz.config.ParameterValue.PValue;
import io.netlibs.zzz.jackson.JsonFunctions;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import software.amazon.awssdk.services.ssm.SsmAsyncClient;

public class Parameters {

  private static SsmAsyncClient ssmManager =
    SsmAsyncClient.builder()
      .build();

  public static Flowable<ParameterValue<PValue>> byPath(String prefix) {
    return Single
      .fromCompletionStage(ssmManager.getParametersByPath(b -> b.path(prefix).withDecryption(true)))
      .toFlowable()
      .flatMapIterable(e -> e.parameters())
      .map(ParameterValue::from)
      .concatWith(Flowable.never());
  }

  public static <T> Flowable<ParameterValue<T>> byPath(String prefix, Class<T> type) {
    Function<String, T> mapper = JsonFunctions.readValue(type);
    return byPath(prefix)
      .mapOptional(e -> e.castOptional(ParameterValue.SecureStringValue.class))
      .map(e -> e.convert(payload -> mapper.apply(payload.secureValue())));
  }

  public static Flowable<ParameterValue<PValue>> forName(String name) {
    return Single
      .fromCompletionStage(ssmManager.getParameter(b -> b.name(name).withDecryption(true)))
      .toFlowable()
      .map(e -> e.parameter())
      .map(ParameterValue::from)
      .concatWith(Flowable.never());
  }

}
