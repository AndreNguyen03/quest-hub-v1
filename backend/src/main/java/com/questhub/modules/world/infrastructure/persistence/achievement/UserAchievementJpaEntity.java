package com.questhub.modules.world.infrastructure.persistence.achievement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_achievements")
@IdClass(UserAchievementId.class)
public class UserAchievementJpaEntity {

  @Id
  @Column(name = "user_id")
  private UUID userId;

  @Id
  @Column(name = "achievement_id")
  private UUID achievementId;

  @Column(name = "unlocked_at", nullable = false)
  private Instant unlockedAt;

  protected UserAchievementJpaEntity() {}

  public UserAchievementJpaEntity(UUID userId, UUID achievementId, Instant unlockedAt) {
    this.userId = userId;
    this.achievementId = achievementId;
    this.unlockedAt = unlockedAt;
  }

  public UUID getUserId() { return userId; }
  public UUID getAchievementId() { return achievementId; }
  public Instant getUnlockedAt() { return unlockedAt; }
}






