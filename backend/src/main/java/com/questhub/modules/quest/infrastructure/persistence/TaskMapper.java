package com.questhub.modules.quest.infrastructure.persistence;

import com.questhub.modules.quest.domain.quest.Resource;
import com.questhub.modules.quest.domain.quest.Task;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class TaskMapper {

  private TaskMapper() {}

  public static TaskEntity toEntity(Task task) {
    TaskEntity entity =
        new TaskEntity(
            task.getId(),
            task.getType(),
            task.getTitle(),
            task.getDescription(),
            task.getOrder(),
            task.getConfig() == null ? Map.of() : task.getConfig());
    for (Resource resource : task.getResources()) {
      ResourceEntity resourceEntity = ResourceMapper.toEntity(resource);
      resourceEntity.setTask(entity);
      entity.getResources().add(resourceEntity);
    }
    return entity;
  }

  public static Task toDomain(TaskEntity entity) {
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