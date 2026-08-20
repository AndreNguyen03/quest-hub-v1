package com.questhub.modules.world.domain;

import java.time.Instant;
import java.util.UUID;

public class World {

  private final UUID id;
  private final UUID userId;
  private final Instant createdAt;

  private World(UUID id, UUID userId, Instant createdAt) {
    this.id = id;
    this.userId = userId;
    this.createdAt = createdAt;
  }

  public static World create(UUID userId) {
    return new World(UUID.randomUUID(), userId, Instant.now());
  }

  public static World restore(UUID id, UUID userId, Instant createdAt) {
    return new World(id, userId, createdAt);
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}