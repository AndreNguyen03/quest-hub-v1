package com.questhub.modules.marketplace.application.dto;

import com.questhub.modules.marketplace.domain.review.Review;
import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(
    UUID id,
    UUID questId,
    UUID userId,
    int score,
    String content,
    Instant createdAt,
    Instant updatedAt) {

  public static ReviewResponse from(Review review) {
    return new ReviewResponse(
        review.getId(),
        review.getQuestId(),
        review.getUserId(),
        review.getScore(),
        review.getContent(),
        review.getCreatedAt(),
        review.getUpdatedAt());
  }
}
