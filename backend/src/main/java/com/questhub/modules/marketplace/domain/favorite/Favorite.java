package com.questhub.modules.marketplace.domain.favorite;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Favorite {

  private final UUID userId;
  private final UUID questId;
  private final Instant createdAt;

  private Favorite(UUID userId, UUID questId, Instant createdAt) {
    this.userId = userId;
    this.questId = questId;
    this.createdAt = createdAt;
  }

  public static Favorite create(UUID userId, UUID questId) {
    Instant now = Instant.now();
    return new Favorite(userId, questId, now);
  }

  public static Favorite restore(UUID userId, UUID questId, Instant createdAt) {
    return new Favorite(userId, questId, createdAt);
  }

  public UUID getUserId() {
    return userId;
  }

  public UUID getQuestId() {
    return questId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
