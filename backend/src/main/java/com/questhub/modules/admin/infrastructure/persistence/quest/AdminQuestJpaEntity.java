package com.questhub.modules.admin.infrastructure.persistence.quest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "quests")
public class AdminQuestJpaEntity {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "creator_id", nullable = false)
  private UUID creatorId;

  @Column(name = "title", nullable = false, length = 120)
  private String title;

  @Column(name = "visibility", nullable = false, length = 10)
  private String visibility;

  @Column(name = "fork_count", nullable = false)
  private int forkCount;

  @Column(name = "avg_rating")
  private BigDecimal avgRating;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected AdminQuestJpaEntity() {}

  public AdminQuestJpaEntity(
      UUID id,
      UUID creatorId,
      String title,
      String visibility,
      int forkCount,
      BigDecimal avgRating,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.creatorId = creatorId;
    this.title = title;
    this.visibility = visibility;
    this.forkCount = forkCount;
    this.avgRating = avgRating;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getCreatorId() {
    return creatorId;
  }

  public String getTitle() {
    return title;
  }

  public String getVisibility() {
    return visibility;
  }

  public int getForkCount() {
    return forkCount;
  }

  public BigDecimal getAvgRating() {
    return avgRating;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
