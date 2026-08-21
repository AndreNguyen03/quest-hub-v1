package com.questhub.modules.admin.infrastructure.persistence.skilldomain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "skill_domains")
public class AdminSkillDomainJpaEntity {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "slug", nullable = false, length = 100)
  private String slug;

  @Column(name = "description", length = 1000)
  private String description;

  @Column(name = "icon", length = 50)
  private String icon;

  @Column(name = "is_active", nullable = false)
  private boolean isActive;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected AdminSkillDomainJpaEntity() {}

  public AdminSkillDomainJpaEntity(
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
