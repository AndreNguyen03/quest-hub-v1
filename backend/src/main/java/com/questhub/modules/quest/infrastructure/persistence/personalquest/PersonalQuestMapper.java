package com.questhub.modules.quest.infrastructure.persistence.personalquest;

import com.questhub.modules.quest.domain.personalquest.PersonalChapter;
import com.questhub.modules.quest.domain.personalquest.PersonalQuest;
import com.questhub.modules.quest.domain.personalquest.PersonalTask;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class PersonalQuestMapper {

  private PersonalQuestMapper() {}

  public static PersonalQuestEntity toEntity(PersonalQuest quest) {
    PersonalQuestEntity entity =
        new PersonalQuestEntity(
            quest.getId(),
            quest.getUserId(),
            quest.getQuestId(),
            quest.getLearningPathId(),
            quest.getTitle(),
            quest.getCompletionRule(),
            quest.getStatus(),
            quest.getProgress(),
            quest.getCreatedAt(),
            quest.getUpdatedAt(),
            quest.getCompletedAt(),
            new ArrayList<>());
    for (PersonalChapter chapter : quest.getChapters()) {
      PersonalChapterEntity chapterEntity = toEntity(chapter);
      chapterEntity.setPersonalQuest(entity);
      entity.getChapters().add(chapterEntity);
    }
    return entity;
  }

  private static PersonalChapterEntity toEntity(PersonalChapter chapter) {
    PersonalChapterEntity entity =
        new PersonalChapterEntity(
            chapter.getId(),
            chapter.getSourceChapterId(),
            chapter.getTitle(),
            chapter.getDescription(),
            chapter.getPosition(),
            chapter.getCreatedAt(),
            chapter.getUpdatedAt(),
            new ArrayList<>());
    for (PersonalTask task : chapter.getTasks()) {
      PersonalTaskEntity taskEntity = toEntity(task);
      taskEntity.setPersonalChapter(entity);
      entity.getTasks().add(taskEntity);
    }
    return entity;
  }

  private static PersonalTaskEntity toEntity(PersonalTask task) {
    return new PersonalTaskEntity(
        task.getId(),
        task.getSourceTaskId(),
        task.getType(),
        task.getTitle(),
        task.getDescription(),
        task.getOrder(),
        task.getConfig(),
        task.isCompleted(),
        task.getCompletedAt(),
        task.getCreatedAt(),
        task.getUpdatedAt());
  }

  public static PersonalQuest toDomain(PersonalQuestEntity entity) {
    List<PersonalChapter> chapters =
        entity.getChapters().stream().map(PersonalQuestMapper::toDomain).collect(Collectors.toList());
    return PersonalQuest.restore(
        entity.getId(),
        entity.getUserId(),
        entity.getQuestId(),
        entity.getLearningPathId(),
        entity.getTitle(),
        entity.getCompletionRule(),
        entity.getStatus(),
        entity.getProgress(),
        chapters,
        entity.getCreatedAt(),
        entity.getUpdatedAt(),
        entity.getCompletedAt());
  }

  private static PersonalChapter toDomain(PersonalChapterEntity entity) {
    List<PersonalTask> tasks =
        entity.getTasks().stream().map(PersonalQuestMapper::toDomain).collect(Collectors.toList());
    return PersonalChapter.restore(
        entity.getId(),
        entity.getSourceChapterId(),
        entity.getTitle(),
        entity.getDescription(),
        entity.getPosition(),
        tasks,
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }

  private static PersonalTask toDomain(PersonalTaskEntity entity) {
    return PersonalTask.restore(
        entity.getId(),
        entity.getSourceTaskId(),
        entity.getType(),
        entity.getTitle(),
        entity.getDescription(),
        entity.getOrder(),
        entity.getConfig(),
        entity.isCompleted(),
        entity.getCompletedAt(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}