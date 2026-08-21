package com.questhub.modules.quest.infrastructure.persistence.learningpath;

import com.questhub.modules.quest.domain.learningpath.LearningPath;

public final class LearningPathMapper {

  private LearningPathMapper() {}

  public static LearningPathJpaEntity toEntity(LearningPath path) {
    return new LearningPathJpaEntity(
        path.getId(),
        path.getDomainId(),
        path.getAuthorId(),
        path.getTitle(),
        path.getDescription(),
        path.getDifficulty(),
        path.getEstimatedDuration(),
        path.isPublic(),
        path.getCreatedAt(),
        path.getUpdatedAt());
  }

  public static LearningPath toDomain(LearningPathJpaEntity entity) {
    return LearningPath.restore(
        entity.getId(),
        entity.getAuthorId(),
        entity.getDomainId(),
        entity.getTitle(),
        entity.getDescription(),
        entity.getDifficulty(),
        entity.getEstimatedDuration(),
        entity.isPublic(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}


