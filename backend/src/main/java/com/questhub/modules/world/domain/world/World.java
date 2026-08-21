package com.questhub.modules.world.domain.world;

import java.time.Instant;
import java.util.UUID;

public class World {

  private final UUID id;
  private final UUID userId;
  private final String username;
  private final Instant createdAt;
  private int questCompletedCount;

  private World(UUID id, UUID userId, String username, Instant createdAt, int questCompletedCount) {
    this.id = id;
    this.userId = userId;
    this.username = username;
    this.createdAt = createdAt;
    this.questCompletedCount = questCompletedCount;
  }

  public static World create(UUID userId, String username) {
    return new World(UUID.randomUUID(), userId, username, Instant.now(), 0);
  }

  public static World restore(UUID id, UUID userId, String username, Instant createdAt, int questCompletedCount) {
    return new World(id, userId, username, createdAt, questCompletedCount);
  }

  public void incrementQuestCount() {
    this.questCompletedCount++;
  }

  public UUID getId() { return id; }
  public UUID getUserId() { return userId; }
  public String getUsername() { return username; }
  public Instant getCreatedAt() { return createdAt; }
  public int getQuestCompletedCount() { return questCompletedCount; }
}
