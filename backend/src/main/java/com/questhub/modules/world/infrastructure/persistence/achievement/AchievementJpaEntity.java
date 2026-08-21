package com.questhub.modules.world.infrastructure.persistence.achievement;

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
@Table(name = "achievements")
public class AchievementJpaEntity {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "code", nullable = false, unique = true)
  private String code;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "description", nullable = false)
  private String description;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "criteria", nullable = false, columnDefinition = "jsonb")
  private Map<String, Object> criteria;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected AchievementJpaEntity() {}

  public AchievementJpaEntity(UUID id, String code, String title, String description, Map<String, Object> criteria, Instant createdAt) {
    this.id = id;
    this.code = code;
    this.title = title;
    this.description = description;
    this.criteria = criteria;
    this.createdAt = createdAt;
  }

  public UUID getId() { return id; }
  public String getCode() { return code; }
  public String getTitle() { return title; }
  public String getDescription() { return description; }
  public Map<String, Object> getCriteria() { return criteria; }
  public Instant getCreatedAt() { return createdAt; }
}






