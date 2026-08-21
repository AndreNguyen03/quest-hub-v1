package com.questhub.modules.world.application.dto;

import com.questhub.modules.world.application.query.GetAchievementsQuery;

public final class AchievementResponseMapper {

  private AchievementResponseMapper() {}

  public static AchievementResponse toResponse(GetAchievementsQuery.AchievementDto dto) {
    return new AchievementResponse(
        dto.id(),
        dto.code(),
        dto.title(),
        dto.description(),
        dto.criteria(),
        dto.unlockedAt(),
        dto.unlocked());
  }
}




