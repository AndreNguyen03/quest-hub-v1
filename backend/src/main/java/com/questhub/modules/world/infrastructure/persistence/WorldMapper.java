package com.questhub.modules.world.infrastructure.persistence;

import com.questhub.modules.world.domain.World;

public final class WorldMapper {

  private WorldMapper() {}

  public static WorldEntity toEntity(World world) {
    return new WorldEntity(world.getId(), world.getUserId(), world.getCreatedAt());
  }

  public static World toDomain(WorldEntity entity) {
    return World.restore(entity.getId(), entity.getUserId(), entity.getCreatedAt());
  }
}