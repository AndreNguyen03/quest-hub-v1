package com.questhub.modules.world.infrastructure.persistence.world;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "worlds")
public class WorldJpaEntity {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "user_id", nullable = false, unique = true)
  private UUID userId;

  @Column(name = "username")
  private String username;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "quest_completed_count", nullable = false)
  private int questCompletedCount;

  protected WorldJpaEntity() {}

  public WorldJpaEntity(UUID id, UUID userId, String username, Instant createdAt, int questCompletedCount) {
    this.id = id;
    this.userId = userId;
    this.username = username;
    this.createdAt = createdAt;
    this.questCompletedCount = questCompletedCount;
  }

  public UUID getId() { return id; }
  public UUID getUserId() { return userId; }
  public String getUsername() { return username; }
  public Instant getCreatedAt() { return createdAt; }
  public int getQuestCompletedCount() { return questCompletedCount; }
}
