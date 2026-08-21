package com.questhub.modules.admin.application.dto;

import com.questhub.modules.admin.domain.featureflag.FeatureFlag;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record FeatureFlagResponse(
    String key,
    Map<String, Object> value,
    String description,
    Instant updatedAt) {

  public static FeatureFlagResponse from(FeatureFlag flag) {
    return new FeatureFlagResponse(flag.getKey(), flag.getValue(), flag.getDescription(), flag.getUpdatedAt());
  }
}
