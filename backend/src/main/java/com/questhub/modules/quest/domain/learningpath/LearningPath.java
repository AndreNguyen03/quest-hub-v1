package com.questhub.modules.quest.domain.learningpath;

import com.questhub.modules.quest.domain.quest.Difficulty;
import java.time.Instant;
import java.util.UUID;

public class LearningPath {

  private final UUID id;
  private final UUID authorId;
  private final UUID domainId;
  private String title;
  private String description;
  private Difficulty difficulty;
  private int estimatedDuration;
  private boolean isPublic;
  private final Instant createdAt;
  private Instant updatedAt;

  private LearningPath(
      UUID id,
      UUID authorId,
      UUID domainId,
      String title,
      String description,
      Difficulty difficulty,
      int estimatedDuration,
      boolean isPublic,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.authorId = authorId;
    this.domainId = domainId;
    this.title = title;
    this.description = description;
    this.difficulty = difficulty;
    this.estimatedDuration = estimatedDuration;
    this.isPublic = isPublic;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static LearningPath create(
      UUID authorId, UUID domainId, String title, String description, Difficulty difficulty) {
    Instant now = Instant.now();
    return new LearningPath(
        UUID.randomUUID(), authorId, domainId, title, description, difficulty, 0, false, now, now);
  }

  public static LearningPath restore(
      UUID id,
      UUID authorId,
      UUID domainId,
      String title,
      String description,
      Difficulty difficulty,
      int estimatedDuration,
      boolean isPublic,
      Instant createdAt,
      Instant updatedAt) {
    return new LearningPath(
        id, authorId, domainId, title, description, difficulty, estimatedDuration, isPublic, createdAt, updatedAt);
  }

  public void update(String title, String description, Difficulty difficulty, boolean isPublic) {
    this.title = title;
    this.description = description;
    this.difficulty = difficulty;
    this.isPublic = isPublic;
    this.updatedAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public UUID getAuthorId() {
    return authorId;
  }

  public UUID getDomainId() {
    return domainId;
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