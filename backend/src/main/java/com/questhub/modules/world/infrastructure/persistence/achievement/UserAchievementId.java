package com.questhub.modules.world.infrastructure.persistence.achievement;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class UserAchievementId implements Serializable {

  private UUID userId;
  private UUID achievementId;

  public UserAchievementId() {}

  public UserAchievementId(UUID userId, UUID achievementId) {
    this.userId = userId;
    this.achievementId = achievementId;
  }

  public UUID getUserId() { return userId; }
  public UUID getAchievementId() { return achievementId; }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof UserAchievementId that)) return false;
    return Objects.equals(userId, that.userId) && Objects.equals(achievementId, that.achievementId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, achievementId);
  }
}





