package com.questhub.modules.quest.infrastructure.persistence.chapter;

import com.questhub.modules.quest.domain.chapter.Chapter;
import com.questhub.modules.quest.domain.task.Task;
import com.questhub.modules.quest.infrastructure.persistence.task.TaskJpaEntity;
import com.questhub.modules.quest.infrastructure.persistence.task.TaskMapper;
import java.util.List;
import java.util.stream.Collectors;

public final class ChapterMapper {

  private ChapterMapper() {}

  public static ChapterJpaEntity toEntity(Chapter chapter) {
    ChapterJpaEntity entity =
        new ChapterJpaEntity(
            chapter.getId(),
            chapter.getTitle(),
            chapter.getDescription(),
            chapter.getPosition());
    for (Task task : chapter.getTasks()) {
      TaskJpaEntity TaskJpaEntity = TaskMapper.toEntity(task);
      TaskJpaEntity.setChapter(entity);
      entity.getTasks().add(TaskJpaEntity);
    }
    return entity;
  }

  public static Chapter toDomain(ChapterJpaEntity entity) {
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




