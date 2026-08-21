package com.questhub.modules.world.infrastructure.persistence.achievement;

import com.questhub.modules.world.domain.achievement.Achievement;

public final class AchievementMapper {

  private AchievementMapper() {}

  public static Achievement toDomain(AchievementJpaEntity entity) {
    return Achievement.restore(
        entity.getId(),
        entity.getCode(),
        entity.getTitle(),
        entity.getDescription(),
        entity.getCriteria(),
        entity.getCreatedAt());
  }

  public static AchievementJpaEntity toEntity(Achievement domain) {
    return new AchievementJpaEntity(
        domain.getId(),
        domain.getCode(),
        domain.getTitle(),
        domain.getDescription(),
        domain.getCriteria(),
        domain.getCreatedAt());
  }
}










