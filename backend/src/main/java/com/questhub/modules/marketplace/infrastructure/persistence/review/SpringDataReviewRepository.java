package com.questhub.modules.marketplace.infrastructure.persistence.review;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataReviewRepository extends JpaRepository<ReviewJpaEntity, UUID> {

  List<ReviewJpaEntity> findByQuestIdOrderByCreatedAtDesc(UUID questId, Pageable pageable);

  List<ReviewJpaEntity> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

  boolean existsByQuestIdAndUserId(UUID questId, UUID userId);

  @Modifying
  @Query("delete from ReviewJpaEntity r where r.questId = :questId and r.userId = :userId")
  void deleteByQuestIdAndUserId(@Param("questId") UUID questId, @Param("userId") UUID userId);
}
