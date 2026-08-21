package com.questhub.modules.marketplace.infrastructure.persistence.review;

import com.questhub.modules.marketplace.domain.review.Review;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reviews")
public class ReviewJpaEntity {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "quest_id", nullable = false)
  private UUID questId;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "score", nullable = false)
  private int score;

  @Column(name = "content", columnDefinition = "TEXT")
  private String content;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected ReviewJpaEntity() {}

  public ReviewJpaEntity(
      UUID id,
      UUID questId,
      UUID userId,
      int score,
      String content,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.questId = questId;
    this.userId = userId;
    this.score = score;
    this.content = content;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getQuestId() {
    return questId;
  }

  public UUID getUserId() {
    return userId;
  }

  public int getScore() {
    return score;
  }

  public String getContent() {
    return content;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
