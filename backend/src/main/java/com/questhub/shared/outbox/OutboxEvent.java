package com.questhub.shared.outbox;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class OutboxEvent {

  private final UUID id;
  private final String aggregateType;
  private final UUID aggregateId;
  private final String eventType;
  private final Map<String, Object> payload;
  private final OutboxStatus status;
  private final int retryCount;
  private final Instant createdAt;
  private final Instant processedAt;

  private OutboxEvent(
      UUID id,
      String aggregateType,
      UUID aggregateId,
      String eventType,
      Map<String, Object> payload,
      OutboxStatus status,
      int retryCount,
      Instant createdAt,
      Instant processedAt) {
    this.id = id;
    this.aggregateType = aggregateType;
    this.aggregateId = aggregateId;
    this.eventType = eventType;
    this.payload = payload;
    this.status = status;
    this.retryCount = retryCount;
    this.createdAt = createdAt;
    this.processedAt = processedAt;
  }

  public static OutboxEvent create(
      String aggregateType, UUID aggregateId, String eventType, Map<String, Object> payload) {
    Instant now = Instant.now();
    return new OutboxEvent(
        UUID.randomUUID(),
        aggregateType,
        aggregateId,
        eventType,
        payload,
        OutboxStatus.PENDING,
        0,
        now,
        null);
  }

  public static OutboxEvent restore(
      UUID id,
      String aggregateType,
      UUID aggregateId,
      String eventType,
      Map<String, Object> payload,
      OutboxStatus status,
      int retryCount,
      Instant createdAt,
      Instant processedAt) {
    return new OutboxEvent(
        id, aggregateType, aggregateId, eventType, payload, status, retryCount, createdAt, processedAt);
  }

  public UUID getId() {
    return id;
  }

  public String getAggregateType() {
    return aggregateType;
  }

  public UUID getAggregateId() {
    return aggregateId;
  }

  public String getEventType() {
    return eventType;
  }

  public Map<String, Object> getPayload() {
    return payload;
  }

  public OutboxStatus getStatus() {
    return status;
  }

  public int getRetryCount() {
    return retryCount;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getProcessedAt() {
    return processedAt;
  }
}