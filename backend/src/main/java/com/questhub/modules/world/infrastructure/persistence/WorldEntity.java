package com.questhub.modules.world.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "worlds")
public class WorldEntity {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "user_id", nullable = false, unique = true)
  private UUID userId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected WorldEntity() {}

  public WorldEntity(UUID id, UUID userId, Instant createdAt) {
    this.id = id;
    this.userId = userId;
    this.createdAt = createdAt;
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