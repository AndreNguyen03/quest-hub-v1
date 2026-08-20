package com.questhub.modules.quest.infrastructure.persistence;

import com.questhub.modules.quest.domain.quest.Chapter;
import com.questhub.modules.quest.domain.quest.Quest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class QuestMapper {

  private QuestMapper() {}

  public static QuestEntity toEntity(Quest quest) {
    QuestEntity entity =
        new QuestEntity(
            quest.getId(),
            quest.getLearningPathId(),
            quest.getCreatorId(),
            quest.getTitle(),
            quest.getDescription(),
            quest.getDifficulty(),
            quest.getEstimatedDuration(),
            quest.getCompletionRule(),
            quest.getReward(),
            quest.getVisibility(),
            quest.getPublishedAt(),
            0,
            null,
            0,
            quest.getCreatedAt(),
            quest.getUpdatedAt(),
            new ArrayList<>());
    for (Chapter chapter : quest.getChapters()) {
      ChapterEntity chapterEntity = ChapterMapper.toEntity(chapter);
      chapterEntity.setQuest(entity);
      entity.getChapters().add(chapterEntity);
    }
    return entity;
  }

  public static Quest toDomain(QuestEntity entity) {
    List<Chapter> chapters =
        entity.getChapters().stream().map(ChapterMapper::toDomain).collect(Collectors.toList());
    return Quest.restore(
        entity.getId(),
        entity.getCreatorId(),
        entity.getLearningPathId(),
        entity.getTitle(),
        entity.getDescription(),
        entity.getDifficulty(),
        entity.getEstimatedDuration(),
        entity.getCompletionRule(),
        entity.getReward(),
        entity.getVisibility(),
        entity.getPublishedAt(),
        chapters,
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}