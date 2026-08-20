package com.questhub.modules.quest.domain.personalquest;

import com.questhub.modules.quest.domain.quest.TaskType;
import com.questhub.shared.domain.DomainValidationException;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class PersonalTask {

  private final UUID id;
  private final UUID sourceTaskId;
  private final TaskType type;
  private String title;
  private String description;
  private int order;
  private Map<String, Object> config;
  private boolean completed;
  private Instant completedAt;
  private final Instant createdAt;
  private Instant updatedAt;

  private PersonalTask(
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
    this.config = config == null ? Map.of() : new LinkedHashMap<>(config);
    this.completed = completed;
    this.completedAt = completedAt;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static PersonalTask create(
      UUID sourceTaskId,
      TaskType type,
      String title,
      String description,
      int order,
      Map<String, Object> config) {
    Instant now = Instant.now();
    return new PersonalTask(
        UUID.randomUUID(), sourceTaskId, type, title, description, order, config, false, null, now, now);
  }

  public static PersonalTask restore(
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
    return new PersonalTask(
        id, sourceTaskId, type, title, description, order, config, completed, completedAt, createdAt, updatedAt);
  }

  public void complete(Map<String, Object> evidence) {
    if (completed) {
      throw new DomainValidationException("Task đã hoàn thành");
    }
    switch (type) {
      case QUIZ -> throw new DomainValidationException("Task QUIZ phải hoàn thành qua làm bài quiz");
      case SUBMISSION -> {
        Object url = evidence == null ? null : evidence.get("url");
        Object text = evidence == null ? null : evidence.get("text");
        if (url == null && text == null) {
          throw new DomainValidationException("Task SUBMISSION bắt buộc nộp url hoặc text vào evidence");
        }
      }
      case REFLECTION -> {
        Object minLengthValue = config.get("minLength");
        int minLength = minLengthValue instanceof Number number ? number.intValue() : 0;
        Object text = evidence == null ? null : evidence.get("text");
        if (text == null || String.valueOf(text).length() < minLength) {
          throw new DomainValidationException(
              "Task REFLECTION bắt buộc text tối thiểu " + minLength + " ký tự");
        }
      }
      case LEARN, PRACTICE -> {}
    }
    this.completed = true;
    this.completedAt = Instant.now();
    this.updatedAt = completedAt;
  }

  public void completeQuiz() {
    if (completed) {
      return;
    }
    this.completed = true;
    this.completedAt = Instant.now();
    this.updatedAt = completedAt;
  }

  public void undo() {
    if (!completed) {
      return;
    }
    this.completed = false;
    this.completedAt = null;
    this.updatedAt = Instant.now();
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

  public void setOrder(int order) {
    this.order = order;
    this.updatedAt = Instant.now();
  }

  public Map<String, Object> getConfig() {
    return Collections.unmodifiableMap(config);
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