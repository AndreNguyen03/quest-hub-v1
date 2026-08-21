package com.questhub.modules.quest.infrastructure.persistence.personalquest;

import com.questhub.modules.quest.domain.personalquest.PersonalChapter;
import com.questhub.modules.quest.domain.personalquest.PersonalQuest;
import com.questhub.modules.quest.domain.personalquest.PersonalTask;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class PersonalQuestMapper {

  private PersonalQuestMapper() {}

  public static PersonalQuestJpaEntity toEntity(PersonalQuest quest) {
    PersonalQuestJpaEntity entity =
        new PersonalQuestJpaEntity(
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
      PersonalChapterJpaEntity ChapterJpaEntity = toEntity(chapter);
      ChapterJpaEntity.setPersonalQuest(entity);
      entity.getChapters().add(ChapterJpaEntity);
    }
    return entity;
  }

  private static PersonalChapterJpaEntity toEntity(PersonalChapter chapter) {
    PersonalChapterJpaEntity entity =
        new PersonalChapterJpaEntity(
            chapter.getId(),
            chapter.getSourceChapterId(),
            chapter.getTitle(),
            chapter.getDescription(),
            chapter.getPosition(),
            chapter.getCreatedAt(),
            chapter.getUpdatedAt(),
            new ArrayList<>());
    for (PersonalTask task : chapter.getTasks()) {
      PersonalTaskJpaEntity TaskJpaEntity = toEntity(task);
      TaskJpaEntity.setPersonalChapter(entity);
      entity.getTasks().add(TaskJpaEntity);
    }
    return entity;
  }

  private static PersonalTaskJpaEntity toEntity(PersonalTask task) {
    return new PersonalTaskJpaEntity(
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

  public static PersonalQuest toDomain(PersonalQuestJpaEntity entity) {
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

  private static PersonalChapter toDomain(PersonalChapterJpaEntity entity) {
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

  private static PersonalTask toDomain(PersonalTaskJpaEntity entity) {
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




