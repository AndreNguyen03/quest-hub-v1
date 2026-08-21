package com.questhub.modules.marketplace.domain.favorite;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavoriteRepository {

  Favorite save(Favorite favorite);

  void deleteByUserIdAndQuestId(UUID userId, UUID questId);

  List<Favorite> findByUserId(UUID userId);

  boolean existsByUserIdAndQuestId(UUID userId, UUID questId);
}
