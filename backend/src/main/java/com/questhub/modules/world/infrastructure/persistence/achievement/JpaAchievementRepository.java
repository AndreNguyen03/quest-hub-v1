package com.questhub.modules.world.infrastructure.persistence.achievement;

import com.questhub.modules.world.domain.achievement.Achievement;
import com.questhub.modules.world.domain.achievement.AchievementRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaAchievementRepository implements AchievementRepository {

  private final SpringDataAchievementRepository jpa;

  @Override
  public List<Achievement> findAll() {
    return jpa.findAll().stream().map(AchievementMapper::toDomain).toList();
  }

  @Override
  public Optional<Achievement> findById(UUID id) {
    return jpa.findById(id).map(AchievementMapper::toDomain);
  }

  @Override
  public Optional<Achievement> findByCode(String code) {
    return jpa.findByCode(code).map(AchievementMapper::toDomain);
  }
}











