package com.questhub.modules.marketplace.infrastructure.persistence.review;

import com.questhub.modules.marketplace.domain.review.Review;
import com.questhub.modules.marketplace.domain.review.ReviewRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class JpaReviewRepository implements ReviewRepository {

  private final SpringDataReviewRepository jpa;

  public JpaReviewRepository(SpringDataReviewRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public Review save(Review review) {
    return ReviewMapper.toDomain(jpa.save(ReviewMapper.toEntity(review)));
  }

  @Override
  public List<Review> findByQuestId(UUID questId) {
    return jpa.findByQuestIdOrderByCreatedAtDesc(questId, PageRequest.of(0, 50)).stream()
        .map(ReviewMapper::toDomain)
        .toList();
  }

  @Override
  public List<Review> findByUserId(UUID userId) {
    return jpa.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 50)).stream()
        .map(ReviewMapper::toDomain)
        .toList();
  }

  @Override
  public boolean existsByQuestIdAndUserId(UUID questId, UUID userId) {
    return jpa.existsByQuestIdAndUserId(questId, userId);
  }

  @Override
  public void delete(UUID questId, UUID userId) {
    jpa.deleteByQuestIdAndUserId(questId, userId);
  }
}
