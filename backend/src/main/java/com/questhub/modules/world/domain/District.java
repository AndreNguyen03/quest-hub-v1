package com.questhub.modules.world.domain;

import java.time.Instant;
import java.util.UUID;

public class District {

  private final UUID id;
  private final UUID worldId;
  private final UUID domainId;
  private int completionCount;
  private final Instant createdAt;
  private Instant updatedAt;

  private District(
      UUID id,
      UUID worldId,
      UUID domainId,
      int completionCount,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.worldId = worldId;
    this.domainId = domainId;
    this.completionCount = completionCount;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static District create(UUID worldId, UUID domainId) {
    Instant now = Instant.now();
    return new District(UUID.randomUUID(), worldId, domainId, 0, now, now);
  }

  public static District restore(
      UUID id,
      UUID worldId,
      UUID domainId,
      int completionCount,
      Instant createdAt,
      Instant updatedAt) {
    return new District(id, worldId, domainId, completionCount, createdAt, updatedAt);
  }

  public void incrementCompletion() {
    this.completionCount++;
    this.updatedAt = Instant.now();
  }

  public void decrementCompletion() {
    if (this.completionCount > 0) {
      this.completionCount--;
    }
    this.updatedAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public UUID getWorldId() {
    return worldId;
  }

  public UUID getDomainId() {
    return domainId;
  }

  public int getCompletionCount() {
    return completionCount;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}