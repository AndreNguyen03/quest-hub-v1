package com.questhub.modules.quest.domain.personalquest;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class TaskCompletion {

  private final UUID id;
  private final UUID personalTaskId;
  private final UUID userId;
  private final Map<String, Object> evidence;
  private final Instant completedAt;
  private final Instant createdAt;

  private TaskCompletion(
      UUID id,
      UUID personalTaskId,
      UUID userId,
      Map<String, Object> evidence,
      Instant completedAt,
      Instant createdAt) {
    this.id = id;
    this.personalTaskId = personalTaskId;
    this.userId = userId;
    this.evidence = evidence == null ? Map.of() : new LinkedHashMap<>(evidence);
    this.completedAt = completedAt;
    this.createdAt = createdAt;
  }

  public static TaskCompletion create(UUID personalTaskId, UUID userId, Map<String, Object> evidence) {
    Instant now = Instant.now();
    return new TaskCompletion(
        UUID.randomUUID(), personalTaskId, userId, evidence, now, now);
  }

  public static TaskCompletion restore(
      UUID id,
      UUID personalTaskId,
      UUID userId,
      Map<String, Object> evidence,
      Instant completedAt,
      Instant createdAt) {
    return new TaskCompletion(id, personalTaskId, userId, evidence, completedAt, createdAt);
  }

  public UUID getId() {
    return id;
  }

  public UUID getPersonalTaskId() {
    return personalTaskId;
  }

  public UUID getUserId() {
    return userId;
  }

  public Map<String, Object> getEvidence() {
    return Collections.unmodifiableMap(evidence);
  }

  public Instant getCompletedAt() {
    return completedAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}