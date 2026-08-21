package com.questhub.modules.admin.domain.featureflag;

import java.time.Instant;
import java.util.Map;

public class FeatureFlag {

  private final String key;
  private Map<String, Object> value;
  private String description;
  private Instant updatedAt;

  private FeatureFlag(String key, Map<String, Object> value, String description, Instant updatedAt) {
    this.key = key;
    this.value = value;
    this.description = description;
    this.updatedAt = updatedAt;
  }

  public static FeatureFlag create(String key, Map<String, Object> value, String description) {
    Instant now = Instant.now();
    return new FeatureFlag(key, value, description, now);
  }

  public static FeatureFlag restore(
      String key, Map<String, Object> value, String description, Instant updatedAt) {
    return new FeatureFlag(key, value, description, updatedAt);
  }

  public void update(Map<String, Object> value, String description) {
    this.value = value;
    this.description = description;
    this.updatedAt = Instant.now();
  }

  public void toggle(Map<String, Object> value) {
    this.value = value;
    this.updatedAt = Instant.now();
  }

  public String getKey() {
    return key;
  }

  public Map<String, Object> getValue() {
    return value;
  }

  public String getDescription() {
    return description;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
