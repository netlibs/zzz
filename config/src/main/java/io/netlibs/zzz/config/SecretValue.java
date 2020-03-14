package io.netlibs.zzz.config;

import java.time.Instant;
import java.util.List;

import org.immutables.value.Value;
import org.immutables.value.Value.Redacted;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.netlibs.zzz.jackson.ObjectMapperFactory;
import io.reactivex.rxjava3.annotations.NonNull;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

@Value.Immutable
public interface SecretValue {

  String secretName();

  String versionId();

  List<String> versionStages();

  String arn();

  Instant createdDate();

  @Redacted
  String secretString();

  default ObjectNode toJson() {
    try {
      return ObjectMapperFactory.objectMapper().readValue(secretString(), ObjectNode.class);
    }
    catch (JsonProcessingException e) {
      throw new RuntimeException (e);
    }
  }

  static @NonNull ImmutableSecretValue valueOf(GetSecretValueResponse value) {
    return ImmutableSecretValue
      .builder()
      .createdDate(value.createdDate())
      .arn(value.arn())
      .secretName(value.name())
      .secretString(value.secretString())
      .versionId(value.versionId())
      .versionStages(value.versionStages())
      .build();
  }

}
