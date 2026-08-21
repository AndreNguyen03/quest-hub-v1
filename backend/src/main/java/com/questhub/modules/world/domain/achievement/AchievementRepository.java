package com.questhub.modules.world.domain.achievement;

import com.questhub.modules.world.domain.achievement.Achievement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AchievementRepository {
  List<Achievement> findAll();
  Optional<Achievement> findById(UUID id);
  Optional<Achievement> findByCode(String code);
}









