package com.questhub.modules.quest.domain.personalquest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class QuizAttempt {

  private final UUID id;
  private final UUID personalTaskId;
  private final UUID userId;
  private final BigDecimal score;
  private final BigDecimal maxScore;
  private final boolean passed;
  private final Map<String, Object> answers;
  private final Instant createdAt;

  private QuizAttempt(
      UUID id,
      UUID personalTaskId,
      UUID userId,
      BigDecimal score,
      BigDecimal maxScore,
      boolean passed,
      Map<String, Object> answers,
      Instant createdAt) {
    this.id = id;
    this.personalTaskId = personalTaskId;
    this.userId = userId;
    this.score = score;
    this.maxScore = maxScore;
    this.passed = passed;
    this.answers = answers == null ? Map.of() : new LinkedHashMap<>(answers);
    this.createdAt = createdAt;
  }

  public static QuizAttempt create(
      UUID personalTaskId,
      UUID userId,
      BigDecimal score,
      BigDecimal maxScore,
      boolean passed,
      Map<String, Object> answers) {
    return new QuizAttempt(
        UUID.randomUUID(), personalTaskId, userId, score, maxScore, passed, answers, Instant.now());
  }

  public static QuizAttempt restore(
      UUID id,
      UUID personalTaskId,
      UUID userId,
      BigDecimal score,
      BigDecimal maxScore,
      boolean passed,
      Map<String, Object> answers,
      Instant createdAt) {
    return new QuizAttempt(id, personalTaskId, userId, score, maxScore, passed, answers, createdAt);
  }

  public UUID getId() {
    return id;
  }

  public UUID getPersonalTaskId() {
    return personalTaskId;
  }

  public UUID getUserId() {
    return userId;
  }

  public BigDecimal getScore() {
    return score;
  }

  public BigDecimal getMaxScore() {
    return maxScore;
  }

  public boolean isPassed() {
    return passed;
  }

  public Map<String, Object> getAnswers() {
    return Collections.unmodifiableMap(answers);
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}