package com.questhub.modules.world.infrastructure.persistence.building;

import com.questhub.modules.world.domain.building.Building;

public final class BuildingMapper {

  private BuildingMapper() {}

  public static BuildingJpaEntity toEntity(Building building) {
    return new BuildingJpaEntity(
        building.getId(),
        building.getDistrictId(),
        building.getType(),
        building.getUnlockedAt(),
        building.getPosition());
  }

  public static Building toDomain(BuildingJpaEntity entity) {
    return Building.restore(
        entity.getId(),
        entity.getDistrictId(),
        entity.getType(),
        entity.getUnlockedAt(),
        entity.getPosition());
  }
}




