package com.questhub.modules.quest.infrastructure.persistence.learningpath;

import com.questhub.modules.quest.domain.quest.Difficulty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "learning_paths")
public class LearningPathJpaEntity {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "domain_id", nullable = false)
  private UUID domainId;

  @Column(name = "author_id", nullable = false)
  private UUID authorId;

  @Column(name = "title", nullable = false, length = 100)
  private String title;

  @Column(name = "description", length = 1000)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "difficulty", nullable = false, length = 20)
  private Difficulty difficulty;

  @Column(name = "estimated_duration", nullable = false)
  private int estimatedDuration;

  @Column(name = "is_public", nullable = false)
  private boolean isPublic;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected LearningPathJpaEntity() {}

  public LearningPathJpaEntity(
      UUID id,
      UUID domainId,
      UUID authorId,
      String title,
      String description,
      Difficulty difficulty,
      int estimatedDuration,
      boolean isPublic,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.domainId = domainId;
    this.authorId = authorId;
    this.title = title;
    this.description = description;
    this.difficulty = difficulty;
    this.estimatedDuration = estimatedDuration;
    this.isPublic = isPublic;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getDomainId() {
    return domainId;
  }

  public UUID getAuthorId() {
    return authorId;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public Difficulty getDifficulty() {
    return difficulty;
  }

  public int getEstimatedDuration() {
    return estimatedDuration;
  }

  public boolean isPublic() {
    return isPublic;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}




