package com.questhub.modules.quest.infrastructure.persistence.task;

import com.questhub.modules.quest.domain.resource.Resource;
import com.questhub.modules.quest.domain.task.Task;
import com.questhub.modules.quest.infrastructure.persistence.resource.ResourceJpaEntity;
import com.questhub.modules.quest.infrastructure.persistence.resource.ResourceMapper;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class TaskMapper {

  private TaskMapper() {}

  public static TaskJpaEntity toEntity(Task task) {
    TaskJpaEntity entity =
        new TaskJpaEntity(
            task.getId(),
            task.getType(),
            task.getTitle(),
            task.getDescription(),
            task.getOrder(),
            task.getConfig() == null ? Map.of() : task.getConfig());
    for (Resource resource : task.getResources()) {
      ResourceJpaEntity ResourceJpaEntity = ResourceMapper.toEntity(resource);
      ResourceJpaEntity.setTask(entity);
      entity.getResources().add(ResourceJpaEntity);
    }
    return entity;
  }

  public static Task toDomain(TaskJpaEntity entity) {
    List<Resource> resources =
        entity.getResources().stream().map(ResourceMapper::toDomain).collect(Collectors.toList());
    return Task.restore(
        entity.getId(),
        entity.getType(),
        entity.getTitle(),
        entity.getDescription(),
        entity.getOrder(),
        entity.getConfig(),
        resources,
        null,
        null);
  }
}




