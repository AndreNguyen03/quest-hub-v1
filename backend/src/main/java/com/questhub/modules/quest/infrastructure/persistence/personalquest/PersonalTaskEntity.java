package com.questhub.modules.quest.infrastructure.persistence.personalquest;

import com.questhub.modules.quest.domain.quest.TaskType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "personal_tasks")
public class PersonalTaskEntity {

  @Id
  @Column(name = "id")
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "personal_chapter_id", nullable = false)
  private PersonalChapterEntity personalChapter;

  @Column(name = "source_task_id")
  private UUID sourceTaskId;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 20)
  private TaskType type;

  @Column(name = "title", nullable = false, length = 100)
  private String title;

  @Column(name = "description", length = 1000)
  private String description;

  @Column(name = "\"order\"", nullable = false)
  private int order;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "config", nullable = false, columnDefinition = "jsonb")
  private Map<String, Object> config;

  @Column(name = "is_completed", nullable = false)
  private boolean completed;

  @Column(name = "completed_at")
  private Instant completedAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected PersonalTaskEntity() {}

  public PersonalTaskEntity(
      UUID id,
      UUID sourceTaskId,
      TaskType type,
      String title,
      String description,
      int order,
      Map<String, Object> config,
      boolean completed,
      Instant completedAt,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.sourceTaskId = sourceTaskId;
    this.type = type;
    this.title = title;
    this.description = description;
    this.order = order;
    this.config = config == null ? Map.of() : config;
    this.completed = completed;
    this.completedAt = completedAt;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public void setPersonalChapter(PersonalChapterEntity personalChapter) {
    this.personalChapter = personalChapter;
  }

  public UUID getId() {
    return id;
  }

  public UUID getSourceTaskId() {
    return sourceTaskId;
  }

  public TaskType getType() {
    return type;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public int getOrder() {
    return order;
  }

  public Map<String, Object> getConfig() {
    return config;
  }

  public boolean isCompleted() {
    return completed;
  }

  public Instant getCompletedAt() {
    return completedAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}