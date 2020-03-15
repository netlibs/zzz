package io.netlibs.zzz.config;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.immutables.value.Value;

import com.google.common.base.Splitter;

import io.netlibs.zzz.config.ImmutableParameterValue.Builder;
import software.amazon.awssdk.services.ssm.model.Parameter;

@Value.Immutable
public interface ParameterValue<T> extends WithParameterValue<T> {

  enum Type {
    STRING,
    STRING_LIST,
    SECURE_STRING,
    UNSUPPORTED
  }

  String name();

  long version();

  Instant lastModified();

  String arn();

  T value();

  //
  interface PValue {

  }

  @Value.Immutable
  interface StringValue extends PValue {

    @Value.Parameter
    String value();

    default Type type() {
      return Type.UNSUPPORTED;
    }

  }

  @Value.Immutable
  interface StringListValue extends PValue {

    @Value.Parameter
    List<String> values();

    default Type type() {
      return Type.STRING_LIST;
    }

  }

  @Value.Immutable
  @Value.Style(redactedMask = "********")
  interface SecureStringValue extends PValue {

    @Value.Parameter
    @Value.Redacted
    String secureValue();

    default Type type() {
      return Type.SECURE_STRING;
    }

  }

  @Value.Immutable
  interface UnsupportedValue extends PValue {

    @Value.Parameter
    String value();

  }

  static ParameterValue<PValue> from(Parameter p) {

    Builder<PValue> b =
      ImmutableParameterValue.<PValue>builder()
        .name(p.name())
        .version(p.version().longValue())
        .lastModified(p.lastModifiedDate())
        .arn(p.arn());

    switch (p.type()) {
      case SECURE_STRING:
        b.value(ImmutableSecureStringValue.of(p.value()));
        break;
      case STRING:
        b.value(ImmutableStringValue.of(p.value()));
        break;
      case STRING_LIST:
        b.value(ImmutableStringListValue.of(Splitter.on(",").splitToList(p.value())));
        break;
      case UNKNOWN_TO_SDK_VERSION:
      default:
        b.value(ImmutableUnsupportedValue.of(p.value()));
        break;
    }

    return b.build();

  }

  default <R> ParameterValue<R> convert(Function<T, R> adapter) {
    return ImmutableParameterValue.<R>builder()
      .name(name())
      .value(adapter.apply(this.value()))
      .version(version())
      .lastModified(lastModified())
      .arn(arn())
      .build();
  }

  default <R> Optional<ParameterValue<R>> castOptional(Class<R> target) {
    if (!target.isInstance(this.value())) {
      return Optional.empty();
    }
    return Optional.of(this.convert(p -> target.cast(p)));
  }

  default <R> ParameterValue<R> cast(Class<R> target) {
    return this.convert(p -> target.cast(p));
  }

}
