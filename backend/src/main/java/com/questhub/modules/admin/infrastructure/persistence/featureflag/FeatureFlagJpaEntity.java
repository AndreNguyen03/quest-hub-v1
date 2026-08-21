package com.questhub.modules.admin.infrastructure.persistence.featureflag;

import com.questhub.modules.admin.domain.featureflag.FeatureFlag;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "feature_flags")
public class FeatureFlagJpaEntity {

  @Id
  @Column(name = "key", length = 100)
  private String key;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "value", nullable = false, columnDefinition = "jsonb")
  private Map<String, Object> value;

  @Column(name = "description")
  private String description;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected FeatureFlagJpaEntity() {}

  public FeatureFlagJpaEntity(
      String key, Map<String, Object> value, String description, Instant updatedAt) {
    this.key = key;
    this.value = value;
    this.description = description;
    this.updatedAt = updatedAt;
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
