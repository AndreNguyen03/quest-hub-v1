package com.questhub.modules.world.domain.achievement;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class Achievement {

  private final UUID id;
  private final String code;
  private final String title;
  private final String description;
  private final Map<String, Object> criteria;
  private final Instant createdAt;

  private Achievement(UUID id, String code, String title, String description, Map<String, Object> criteria, Instant createdAt) {
    this.id = id;
    this.code = code;
    this.title = title;
    this.description = description;
    this.criteria = criteria;
    this.createdAt = createdAt;
  }

  public static Achievement restore(UUID id, String code, String title, String description, Map<String, Object> criteria, Instant createdAt) {
    return new Achievement(id, code, title, description, criteria, createdAt);
  }

  public UUID getId() { return id; }
  public String getCode() { return code; }
  public String getTitle() { return title; }
  public String getDescription() { return description; }
  public Map<String, Object> getCriteria() { return criteria; }
  public Instant getCreatedAt() { return createdAt; }
}







