package com.questhub.modules.admin.infrastructure.persistence.featureflag;

import com.questhub.modules.admin.domain.featureflag.FeatureFlag;

public final class FeatureFlagMapper {

  private FeatureFlagMapper() {}

  public static FeatureFlag toDomain(FeatureFlagJpaEntity entity) {
    return FeatureFlag.restore(
        entity.getKey(), entity.getValue(), entity.getDescription(), entity.getUpdatedAt());
  }
}
