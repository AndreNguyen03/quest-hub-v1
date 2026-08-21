package com.questhub.modules.world.infrastructure.persistence.district;

import com.questhub.modules.world.domain.district.District;

public final class DistrictMapper {

  private DistrictMapper() {}

  public static DistrictJpaEntity toEntity(District district) {
    return new DistrictJpaEntity(
        district.getId(),
        district.getWorldId(),
        district.getDomainId(),
        district.getCompletionCount(),
        district.getCreatedAt(),
        district.getUpdatedAt());
  }

  public static District toDomain(DistrictJpaEntity entity) {
    return District.restore(
        entity.getId(),
        entity.getWorldId(),
        entity.getDomainId(),
        entity.getCompletionCount(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}




