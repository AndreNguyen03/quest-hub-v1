package com.questhub.modules.quest.infrastructure.persistence.personalquest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "quiz_attempts")
public class QuizAttemptJpaEntity {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "personal_task_id", nullable = false)
  private UUID personalTaskId;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "score", nullable = false, precision = 5, scale = 2)
  private BigDecimal score;

  @Column(name = "max_score", nullable = false, precision = 5, scale = 2)
  private BigDecimal maxScore;

  @Column(name = "passed", nullable = false)
  private boolean passed;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "answers", nullable = false, columnDefinition = "jsonb")
  private Map<String, Object> answers;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected QuizAttemptJpaEntity() {}

  public QuizAttemptJpaEntity(
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
    this.answers = answers == null ? Map.of() : answers;
    this.createdAt = createdAt;
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
    return answers;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}




