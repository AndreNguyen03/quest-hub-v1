package com.questhub.modules.marketplace.infrastructure.persistence.favorite;

import com.questhub.modules.marketplace.domain.favorite.Favorite;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataFavoriteRepository extends JpaRepository<FavoriteJpaEntity, FavoriteId> {

  List<FavoriteJpaEntity> findByIdUserIdOrderByCreatedAtDesc(UUID userId);

  boolean existsByIdUserIdAndIdQuestId(UUID userId, UUID questId);

  @Modifying
  @Query("delete from FavoriteJpaEntity f where f.id.userId = :userId and f.id.questId = :questId")
  void deleteByIdUserIdAndIdQuestId(@Param("userId") UUID userId, @Param("questId") UUID questId);
}
