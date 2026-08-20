package com.questhub.shared.outbox;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataOutboxEventRepository extends JpaRepository<OutboxEventEntity, UUID> {

  @Query(
      value =
          "SELECT * FROM outbox_events WHERE status = 'PENDING' "
              + "ORDER BY created_at LIMIT :limit FOR UPDATE SKIP LOCKED",
      nativeQuery = true)
  List<OutboxEventEntity> findPending(@Param("limit") int limit);

  @Modifying
  @Query("UPDATE OutboxEventEntity e SET e.status = :status WHERE e.id = :id")
  void markProcessing(@Param("id") UUID id, @Param("status") OutboxStatus status);

  @Modifying
  @Query("UPDATE OutboxEventEntity e SET e.status = :status, e.processedAt = :processedAt WHERE e.id = :id")
  void markProcessed(
      @Param("id") UUID id, @Param("status") OutboxStatus status, @Param("processedAt") Instant processedAt);

  @Modifying
  @Query("UPDATE OutboxEventEntity e SET e.status = :status, e.retryCount = :retryCount WHERE e.id = :id")
  void markFailed(@Param("id") UUID id, @Param("status") OutboxStatus status, @Param("retryCount") int retryCount);

  @Modifying
  @Query("UPDATE OutboxEventEntity e SET e.status = :status, e.retryCount = :retryCount WHERE e.id = :id")
  void markPending(@Param("id") UUID id, @Param("status") OutboxStatus status, @Param("retryCount") int retryCount);
}