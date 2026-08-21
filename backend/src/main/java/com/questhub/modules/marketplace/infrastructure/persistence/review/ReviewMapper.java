package com.questhub.modules.marketplace.infrastructure.persistence.review;

import com.questhub.modules.marketplace.domain.review.Review;

public final class ReviewMapper {

  private ReviewMapper() {}

  public static ReviewJpaEntity toEntity(Review review) {
    return new ReviewJpaEntity(
        review.getId(),
        review.getQuestId(),
        review.getUserId(),
        review.getScore(),
        review.getContent(),
        review.getCreatedAt(),
        review.getUpdatedAt());
  }

  public static Review toDomain(ReviewJpaEntity entity) {
    return Review.restore(
        entity.getId(),
        entity.getQuestId(),
        entity.getUserId(),
        entity.getScore(),
        entity.getContent(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
