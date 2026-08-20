package com.questhub.modules.world.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "districts")
public class DistrictEntity {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "world_id", nullable = false)
  private UUID worldId;

  @Column(name = "domain_id", nullable = false)
  private UUID domainId;

  @Column(name = "completion_count", nullable = false)
  private int completionCount;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected DistrictEntity() {}

  public DistrictEntity(
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