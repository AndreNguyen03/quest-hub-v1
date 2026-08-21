package com.questhub.modules.quest.infrastructure.persistence.quest;

import com.questhub.modules.quest.domain.chapter.Chapter;
import com.questhub.modules.quest.domain.quest.Quest;
import com.questhub.modules.quest.infrastructure.persistence.chapter.ChapterJpaEntity;
import com.questhub.modules.quest.infrastructure.persistence.chapter.ChapterMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class QuestMapper {

  private QuestMapper() {}

  public static QuestJpaEntity toEntity(Quest quest) {
    QuestJpaEntity entity =
        new QuestJpaEntity(
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
            quest.getForkCount(),
            quest.getAvgRating(),
            quest.getRatingCount(),
            quest.getCreatedAt(),
            quest.getUpdatedAt(),
            new ArrayList<>());
    for (Chapter chapter : quest.getChapters()) {
      ChapterJpaEntity ChapterJpaEntity = ChapterMapper.toEntity(chapter);
      ChapterJpaEntity.setQuest(entity);
      entity.getChapters().add(ChapterJpaEntity);
    }
    return entity;
  }

  public static Quest toDomain(QuestJpaEntity entity) {
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
        entity.getForkCount(),
        entity.getAvgRating(),
        entity.getRatingCount(),
        chapters,
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}




