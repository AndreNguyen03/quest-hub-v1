package com.questhub.modules.marketplace.domain.review;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository {

  Review save(Review review);

  List<Review> findByQuestId(UUID questId);

  List<Review> findByUserId(UUID userId);

  boolean existsByQuestIdAndUserId(UUID questId, UUID userId);

  void delete(UUID questId, UUID userId);
}
