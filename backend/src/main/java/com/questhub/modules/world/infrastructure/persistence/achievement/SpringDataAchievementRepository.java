package com.questhub.modules.world.infrastructure.persistence.achievement;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAchievementRepository extends JpaRepository<AchievementJpaEntity, UUID> {
  Optional<AchievementJpaEntity> findByCode(String code);
}






