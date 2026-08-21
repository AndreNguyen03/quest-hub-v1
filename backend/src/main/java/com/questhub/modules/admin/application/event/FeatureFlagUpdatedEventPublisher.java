package com.questhub.modules.admin.application.event;

import com.questhub.modules.admin.domain.featureflag.FeatureFlag;
import com.questhub.shared.outbox.OutboxPublisher;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeatureFlagUpdatedEventPublisher {

  private final OutboxPublisher outboxPublisher;

  public void publish(FeatureFlag featureFlag) {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("key", featureFlag.getKey());
    payload.put("value", featureFlag.getValue());
    payload.put("description", featureFlag.getDescription());

    UUID aggregateId = UUID.nameUUIDFromBytes(featureFlag.getKey().getBytes());
    outboxPublisher.publish("FeatureFlag", aggregateId, "feature_flag.updated", payload);
  }
}
