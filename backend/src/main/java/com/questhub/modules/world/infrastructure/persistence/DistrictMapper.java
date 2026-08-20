package com.questhub.modules.world.infrastructure.persistence;

import com.questhub.modules.world.domain.District;

public final class DistrictMapper {

  private DistrictMapper() {}

  public static DistrictEntity toEntity(District district) {
    return new DistrictEntity(
        district.getId(),
        district.getWorldId(),
        district.getDomainId(),
        district.getCompletionCount(),
        district.getCreatedAt(),
        district.getUpdatedAt());
  }

  public static District toDomain(DistrictEntity entity) {
    return District.restore(
        entity.getId(),
        entity.getWorldId(),
        entity.getDomainId(),
        entity.getCompletionCount(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}