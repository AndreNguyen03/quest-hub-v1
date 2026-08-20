package com.questhub.shared.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "outbox_events")
public class OutboxEventEntity {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "aggregate_type", nullable = false)
  private String aggregateType;

  @Column(name = "aggregate_id", nullable = false)
  private UUID aggregateId;

  @Column(name = "event_type", nullable = false)
  private String eventType;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
  private Map<String, Object> payload;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 10)
  private OutboxStatus status;

  @Column(name = "retry_count", nullable = false)
  private int retryCount;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "processed_at")
  private Instant processedAt;

  protected OutboxEventEntity() {}

  public OutboxEventEntity(
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