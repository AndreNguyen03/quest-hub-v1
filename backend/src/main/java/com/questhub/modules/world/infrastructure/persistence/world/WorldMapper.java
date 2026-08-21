package com.questhub.modules.world.infrastructure.persistence.world;

import com.questhub.modules.world.domain.world.World;

public final class WorldMapper {

  private WorldMapper() {}

  public static WorldJpaEntity toEntity(World world) {
    return new WorldJpaEntity(
        world.getId(), world.getUserId(), world.getUsername(),
        world.getCreatedAt(), world.getQuestCompletedCount());
  }

  public static World toDomain(WorldJpaEntity entity) {
    return World.restore(
        entity.getId(), entity.getUserId(), entity.getUsername(),
        entity.getCreatedAt(), entity.getQuestCompletedCount());
  }
}




