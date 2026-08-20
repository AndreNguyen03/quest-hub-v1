package com.questhub.shared.outbox;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class OutboxEventRepositoryImpl implements OutboxEventRepository {

  private final SpringDataOutboxEventRepository jpa;

  public OutboxEventRepositoryImpl(SpringDataOutboxEventRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public OutboxEvent save(OutboxEvent event) {
    return OutboxEventMapper.toDomain(jpa.save(OutboxEventMapper.toEntity(event)));
  }

  @Override
  public List<OutboxEvent> findPending(int limit) {
    return jpa.findPending(limit).stream().map(OutboxEventMapper::toDomain).toList();
  }

  @Override
  public void markProcessing(UUID id) {
    jpa.markProcessing(id, OutboxStatus.PROCESSING);
  }

  @Override
  public void markProcessed(UUID id) {
    jpa.markProcessed(id, OutboxStatus.PROCESSED, Instant.now());
  }

  @Override
  public void markFailed(UUID id, int retryCount) {
    jpa.markFailed(id, OutboxStatus.FAILED, retryCount);
  }

  @Override
  public void markPending(UUID id, int retryCount) {
    jpa.markPending(id, OutboxStatus.PENDING, retryCount);
  }
}