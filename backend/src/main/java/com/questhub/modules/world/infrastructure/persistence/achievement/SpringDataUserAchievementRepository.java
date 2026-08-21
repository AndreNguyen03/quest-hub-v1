package com.questhub.modules.world.infrastructure.persistence.achievement;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataUserAchievementRepository extends JpaRepository<UserAchievementJpaEntity, UserAchievementId> {

  List<UserAchievementJpaEntity> findByUserId(UUID userId);

  boolean existsByUserIdAndAchievementId(UUID userId, UUID achievementId);

  @Query("select ua.achievementId from UserAchievementJpaEntity ua where ua.userId = :userId")
  List<UUID> findAchievementIdsByUserId(@Param("userId") UUID userId);
}






