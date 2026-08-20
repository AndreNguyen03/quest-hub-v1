package com.questhub.modules.quest.infrastructure.persistence;

import com.questhub.modules.quest.domain.quest.Resource;

public final class ResourceMapper {

  private ResourceMapper() {}

  public static ResourceEntity toEntity(Resource resource) {
    return new ResourceEntity(
        resource.getId(),
        resource.getType(),
        resource.getTitle(),
        resource.getUrl(),
        resource.getEstimatedMinutes());
  }

  public static Resource toDomain(ResourceEntity entity) {
    return Resource.restore(
        entity.getId(),
        entity.getType(),
        entity.getTitle(),
        entity.getUrl(),
        entity.getEstimatedMinutes(),
        null,
        null);
  }
}