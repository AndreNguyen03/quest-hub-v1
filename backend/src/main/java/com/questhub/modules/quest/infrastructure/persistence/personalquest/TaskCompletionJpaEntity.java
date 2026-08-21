package com.questhub.modules.quest.infrastructure.persistence.personalquest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "task_completions")
public class TaskCompletionJpaEntity {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "personal_task_id", nullable = false, unique = true)
  private UUID personalTaskId;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "evidence", nullable = false, columnDefinition = "jsonb")
  private Map<String, Object> evidence;

  @Column(name = "completed_at", nullable = false)
  private Instant completedAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected TaskCompletionJpaEntity() {}

  public TaskCompletionJpaEntity(
      UUID id,
      UUID personalTaskId,
      UUID userId,
      Map<String, Object> evidence,
      Instant completedAt,
      Instant createdAt) {
    this.id = id;
    this.personalTaskId = personalTaskId;
    this.userId = userId;
    this.evidence = evidence == null ? Map.of() : evidence;
    this.completedAt = completedAt;
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

  public Map<String, Object> getEvidence() {
    return evidence;
  }

  public Instant getCompletedAt() {
    return completedAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}




