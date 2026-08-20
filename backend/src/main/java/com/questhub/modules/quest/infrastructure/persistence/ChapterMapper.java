package com.questhub.modules.quest.infrastructure.persistence;

import com.questhub.modules.quest.domain.quest.Chapter;
import com.questhub.modules.quest.domain.quest.Task;
import java.util.List;
import java.util.stream.Collectors;

public final class ChapterMapper {

  private ChapterMapper() {}

  public static ChapterEntity toEntity(Chapter chapter) {
    ChapterEntity entity =
        new ChapterEntity(
            chapter.getId(),
            chapter.getTitle(),
            chapter.getDescription(),
            chapter.getPosition());
    for (Task task : chapter.getTasks()) {
      TaskEntity taskEntity = TaskMapper.toEntity(task);
      taskEntity.setChapter(entity);
      entity.getTasks().add(taskEntity);
    }
    return entity;
  }

  public static Chapter toDomain(ChapterEntity entity) {
    List<Task> tasks =
        entity.getTasks().stream().map(TaskMapper::toDomain).collect(Collectors.toList());
    return Chapter.restore(
        entity.getId(),
        entity.getTitle(),
        entity.getDescription(),
        entity.getPosition(),
        tasks,
        null,
        null);
  }
}