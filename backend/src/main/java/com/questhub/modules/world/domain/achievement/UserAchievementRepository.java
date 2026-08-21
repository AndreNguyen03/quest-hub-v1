package com.questhub.modules.world.domain.achievement;

import com.questhub.modules.world.domain.achievement.UserAchievement;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface UserAchievementRepository {
  List<UserAchievement> findByUserId(UUID userId);
  boolean existsByUserIdAndAchievementId(UUID userId, UUID achievementId);
  UserAchievement save(UserAchievement userAchievement);
  Set<UUID> findAchievementIdsByUserId(UUID userId);
}









