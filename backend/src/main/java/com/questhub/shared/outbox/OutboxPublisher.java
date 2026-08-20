package com.questhub.shared.outbox;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class OutboxPublisher {

  private static final String EVENT_VERSION = "1.0";

  private final OutboxEventRepository outboxEventRepository;

  public OutboxPublisher(OutboxEventRepository outboxEventRepository) {
    this.outboxEventRepository = outboxEventRepository;
  }

  @Transactional(
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public void publish(String aggregateType, UUID aggregateId, String eventType, Map<String, Object> fields) {
    UUID eventId = UUID.randomUUID();
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("eventId", eventId.toString());
    payload.put("timestamp", Instant.now().toString());
    payload.put("version", EVENT_VERSION);
    payload.putAll(fields);

    OutboxEvent event = outboxEventRepository.save(
        OutboxEvent.create(aggregateType, aggregateId, eventType, payload));
    log.info("Outbox event stored eventId={} type={}", event.getId(), event.getEventType());
  }
}