package com.questhub.modules.world.domain.achievement;

import java.time.Instant;
import java.util.UUID;

public class UserAchievement {

  private final UUID userId;
  private final UUID achievementId;
  private final Instant unlockedAt;

  public UserAchievement(UUID userId, UUID achievementId, Instant unlockedAt) {
    this.userId = userId;
    this.achievementId = achievementId;
    this.unlockedAt = unlockedAt;
  }

  public static UserAchievement create(UUID userId, UUID achievementId) {
    return new UserAchievement(userId, achievementId, Instant.now());
  }

  public UUID getUserId() { return userId; }
  public UUID getAchievementId() { return achievementId; }
  public Instant getUnlockedAt() { return unlockedAt; }
}








