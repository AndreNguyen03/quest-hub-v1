package com.questhub.modules.admin.domain.skilldomain;

import java.time.Instant;
import java.util.UUID;

public class AdminSkillDomain {

  private final UUID id;
  private String name;
  private String slug;
  private String description;
  private String icon;
  private boolean isActive;
  private final Instant createdAt;
  private Instant updatedAt;

  private AdminSkillDomain(
      UUID id,
      String name,
      String slug,
      String description,
      String icon,
      boolean isActive,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.name = name;
    this.slug = slug;
    this.description = description;
    this.icon = icon;
    this.isActive = isActive;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static AdminSkillDomain create(
      UUID id, String name, String slug, String description, String icon) {
    Instant now = Instant.now();
    return new AdminSkillDomain(id, name, slug, description, icon, true, now, now);
  }

  public static AdminSkillDomain restore(
      UUID id,
      String name,
      String slug,
      String description,
      String icon,
      boolean isActive,
      Instant createdAt,
      Instant updatedAt) {
    return new AdminSkillDomain(id, name, slug, description, icon, isActive, createdAt, updatedAt);
  }

  public void update(String name, String slug, String description, String icon) {
    this.name = name;
    this.slug = slug;
    this.description = description;
    this.icon = icon;
    this.updatedAt = Instant.now();
  }

  public void deactivate() {
    this.isActive = false;
    this.updatedAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getSlug() {
    return slug;
  }

  public String getDescription() {
    return description;
  }

  public String getIcon() {
    return icon;
  }

  public boolean isActive() {
    return isActive;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
