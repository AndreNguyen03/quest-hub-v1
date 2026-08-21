package com.questhub.shared.outbox;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxRelay {

  private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

  private static final int MAX_RETRY = 5;
  private static final int BATCH_SIZE = 100;

  private final OutboxEventRepository outboxEventRepository;
  private final OutboxDispatcher outboxDispatcher;

  public OutboxRelay(OutboxEventRepository outboxEventRepository, OutboxDispatcher outboxDispatcher) {
    this.outboxEventRepository = outboxEventRepository;
    this.outboxDispatcher = outboxDispatcher;
  }

  @Scheduled(fixedDelayString = "${app.outbox.relay-fixed-delay:1000}")
  public void relay() {
    List<OutboxEvent> pending;
    try {
      pending = outboxEventRepository.findPending(BATCH_SIZE);
    } catch (Exception ex) {
      log.warn("Outbox relay could not query pending events", ex);
      return;
    }
    for (OutboxEvent event : pending) {
      try {
        outboxDispatcher.dispatch(event.getId(), event.getEventType(), event.getPayload());
        log.info("Outbox event dispatched eventId={} type={}", event.getId(), event.getEventType());
      } catch (Exception ex) {
        int retryCount = event.getRetryCount() + 1;
        if (retryCount >= MAX_RETRY) {
          outboxEventRepository.markFailed(event.getId(), retryCount);
          log.error("Outbox event FAILED eventId={} type={} retryCount={}", event.getId(), event.getEventType(), retryCount, ex);
        } else {
          outboxEventRepository.markPending(event.getId(), retryCount);
          log.warn("Outbox dispatch failed, will retry eventId={} type={} retryCount={}", event.getId(), event.getEventType(), retryCount, ex);
        }
      }
    }
  }
}