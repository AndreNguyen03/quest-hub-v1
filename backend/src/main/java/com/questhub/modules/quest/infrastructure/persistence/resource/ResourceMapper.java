package com.questhub.modules.quest.infrastructure.persistence.resource;

import com.questhub.modules.quest.domain.resource.Resource;

public final class ResourceMapper {

  private ResourceMapper() {}

  public static ResourceJpaEntity toEntity(Resource resource) {
    return new ResourceJpaEntity(
        resource.getId(),
        resource.getType(),
        resource.getTitle(),
        resource.getUrl(),
        resource.getEstimatedMinutes());
  }

  public static Resource toDomain(ResourceJpaEntity entity) {
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




