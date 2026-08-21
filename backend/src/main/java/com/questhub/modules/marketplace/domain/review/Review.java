package com.questhub.modules.marketplace.domain.review;

import java.time.Instant;
import java.util.UUID;

public class Review {

  private final UUID id;
  private final UUID questId;
  private final UUID userId;
  private int score;
  private String content;
  private final Instant createdAt;
  private Instant updatedAt;

  private Review(
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

  public static Review create(UUID questId, UUID userId, int score, String content) {
    Instant now = Instant.now();
    return new Review(UUID.randomUUID(), questId, userId, score, content, now, now);
  }

  public static Review restore(
      UUID id,
      UUID questId,
      UUID userId,
      int score,
      String content,
      Instant createdAt,
      Instant updatedAt) {
    return new Review(id, questId, userId, score, content, createdAt, updatedAt);
  }

  public void update(int score, String content) {
    this.score = score;
    this.content = content;
    this.updatedAt = Instant.now();
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
