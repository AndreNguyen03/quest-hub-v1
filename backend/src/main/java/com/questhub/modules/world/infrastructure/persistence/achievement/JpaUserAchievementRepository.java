package com.questhub.modules.world.infrastructure.persistence.achievement;

import com.questhub.modules.world.domain.achievement.UserAchievement;
import com.questhub.modules.world.domain.achievement.UserAchievementRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaUserAchievementRepository implements UserAchievementRepository {

  private final SpringDataUserAchievementRepository jpa;

  @Override
  public List<UserAchievement> findByUserId(UUID userId) {
    return jpa.findByUserId(userId).stream().map(UserAchievementMapper::toDomain).toList();
  }

  @Override
  public boolean existsByUserIdAndAchievementId(UUID userId, UUID achievementId) {
    return jpa.existsByUserIdAndAchievementId(userId, achievementId);
  }

  @Override
  public UserAchievement save(UserAchievement userAchievement) {
    jpa.save(UserAchievementMapper.toEntity(userAchievement));
    return userAchievement;
  }

  @Override
  public Set<UUID> findAchievementIdsByUserId(UUID userId) {
    return new HashSet<>(jpa.findAchievementIdsByUserId(userId));
  }
}











