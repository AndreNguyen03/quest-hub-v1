package com.questhub.shared.outbox;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository {

  OutboxEvent save(OutboxEvent event);

  List<OutboxEvent> findPending(int limit);

  void markProcessing(UUID id);

  void markProcessed(UUID id);

  void markFailed(UUID id, int retryCount);

  void markPending(UUID id, int retryCount);
}