package com.questhub.shared.outbox;

import java.util.Map;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OutboxDispatcher {

  private final OutboxEventRepository outboxEventRepository;
  private final ApplicationEventPublisher applicationEventPublisher;

  public OutboxDispatcher(
      OutboxEventRepository outboxEventRepository,
      ApplicationEventPublisher applicationEventPublisher) {
    this.outboxEventRepository = outboxEventRepository;
    this.applicationEventPublisher = applicationEventPublisher;
  }

  @Transactional(
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRES_NEW)
  public void dispatch(UUID id, String eventType, Map<String, Object> payload) {
    outboxEventRepository.markProcessing(id);
    applicationEventPublisher.publishEvent(new OutboxEventDispatched(id, eventType, payload));
    outboxEventRepository.markProcessed(id);
  }
}