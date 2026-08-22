package com.questhub.modules.marketplace.application.event;

import com.questhub.shared.outbox.OutboxPublisher;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuestRatedEventPublisher {

  private final OutboxPublisher outboxPublisher;

  public void publish(UUID questId, UUID userId, int newScore, Integer previousScore, String action) {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("questId", questId.toString());
    payload.put("userId", userId.toString());
    payload.put("newScore", newScore);
    payload.put("previousScore", previousScore);
    payload.put("action", action);

    outboxPublisher.publish("Quest", questId, "quest.rated", payload);
  }
}
