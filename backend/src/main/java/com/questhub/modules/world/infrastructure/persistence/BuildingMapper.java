package com.questhub.modules.world.infrastructure.persistence;

import com.questhub.modules.world.domain.Building;

public final class BuildingMapper {

  private BuildingMapper() {}

  public static BuildingEntity toEntity(Building building) {
    return new BuildingEntity(
        building.getId(),
        building.getDistrictId(),
        building.getType(),
        building.getUnlockedAt(),
        building.getPosition());
  }

  public static Building toDomain(BuildingEntity entity) {
    return Building.restore(
        entity.getId(),
        entity.getDistrictId(),
        entity.getType(),
        entity.getUnlockedAt(),
        entity.getPosition());
  }
}