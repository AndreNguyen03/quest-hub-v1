package com.questhub.modules.quest.domain.quest;

import java.time.Instant;
import java.util.UUID;

public class Resource {

  private final UUID id;
  private final ResourceType type;
  private final String title;
  private final String url;
  private final Integer estimatedMinutes;
  private final Instant createdAt;
  private final Instant updatedAt;

  private Resource(
      UUID id,
      ResourceType type,
      String title,
      String url,
      Integer estimatedMinutes,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.type = type;
    this.title = title;
    this.url = url;
    this.estimatedMinutes = estimatedMinutes;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static Resource create(
      ResourceType type, String title, String url, Integer estimatedMinutes) {
    Instant now = Instant.now();
    return new Resource(UUID.randomUUID(), type, title, url, estimatedMinutes, now, now);
  }

  public static Resource restore(
      UUID id,
      ResourceType type,
      String title,
      String url,
      Integer estimatedMinutes,
      Instant createdAt,
      Instant updatedAt) {
    return new Resource(id, type, title, url, estimatedMinutes, createdAt, updatedAt);
  }

  public UUID getId() {
    return id;
  }

  public ResourceType getType() {
    return type;
  }

  public String getTitle() {
    return title;
  }

  public String getUrl() {
    return url;
  }

  public Integer getEstimatedMinutes() {
    return estimatedMinutes;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}