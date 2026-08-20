package com.questhub.modules.quest.domain.quest;

import com.questhub.shared.domain.DomainValidationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Task {

  private final UUID id;
  private final TaskType type;
  private String title;
  private String description;
  private int order;
  private Map<String, Object> config;
  private final List<Resource> resources = new ArrayList<>();
  private final Instant createdAt;
  private Instant updatedAt;

  private Task(
      UUID id,
      TaskType type,
      String title,
      String description,
      int order,
      Map<String, Object> config,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.type = type;
    this.title = title;
    this.description = description;
    this.order = order;
    this.config = config == null ? Map.of() : config;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static Task create(
      TaskType type, String title, String description, int order, Map<String, Object> config) {
    Instant now = Instant.now();
    return new Task(UUID.randomUUID(), type, title, description, order, config, now, now);
  }

  public static Task restore(
      UUID id,
      TaskType type,
      String title,
      String description,
      int order,
      Map<String, Object> config,
      List<Resource> resources,
      Instant createdAt,
      Instant updatedAt) {
    Task task = new Task(id, type, title, description, order, config, createdAt, updatedAt);
    task.resources.addAll(resources);
    return task;
  }

  public void addResource(Resource resource) {
    if (type != TaskType.LEARN) {
      throw new DomainValidationException("Only LEARN tasks can have resources");
    }
    resources.add(resource);
    this.updatedAt = Instant.now();
  }

  public void removeResource(UUID resourceId) {
    resources.removeIf(resource -> resource.getId().equals(resourceId));
    this.updatedAt = Instant.now();
  }

  public void changeOrder(int order) {
    this.order = order;
    this.updatedAt = Instant.now();
  }

  public void updateDetails(String title, String description, Map<String, Object> config) {
    this.title = title;
    this.description = description;
    this.config = config == null ? Map.of() : config;
    this.updatedAt = Instant.now();
  }

  public UUID getId() {
    return id;
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

  public List<Resource> getResources() {
    return Collections.unmodifiableList(resources);
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}