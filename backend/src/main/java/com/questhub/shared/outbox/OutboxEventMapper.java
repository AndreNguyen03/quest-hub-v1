package com.questhub.shared.outbox;

public final class OutboxEventMapper {

  private OutboxEventMapper() {}

  public static OutboxEventEntity toEntity(OutboxEvent event) {
    return new OutboxEventEntity(
        event.getId(),
        event.getAggregateType(),
        event.getAggregateId(),
        event.getEventType(),
        event.getPayload(),
        event.getStatus(),
        event.getRetryCount(),
        event.getCreatedAt(),
        event.getProcessedAt());
  }

  public static OutboxEvent toDomain(OutboxEventEntity entity) {
    return OutboxEvent.restore(
        entity.getId(),
        entity.getAggregateType(),
        entity.getAggregateId(),
        entity.getEventType(),
        entity.getPayload(),
        entity.getStatus(),
        entity.getRetryCount(),
        entity.getCreatedAt(),
        entity.getProcessedAt());
  }
}