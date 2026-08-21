package com.questhub.modules.world.infrastructure.persistence.achievement;

import com.questhub.modules.world.domain.achievement.UserAchievement;

public final class UserAchievementMapper {

  private UserAchievementMapper() {}

  public static UserAchievement toDomain(UserAchievementJpaEntity entity) {
    return new UserAchievement(entity.getUserId(), entity.getAchievementId(), entity.getUnlockedAt());
  }

  public static UserAchievementJpaEntity toEntity(UserAchievement domain) {
    return new UserAchievementJpaEntity(domain.getUserId(), domain.getAchievementId(), domain.getUnlockedAt());
  }
}










