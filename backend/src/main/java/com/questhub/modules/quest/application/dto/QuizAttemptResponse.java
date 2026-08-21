package com.questhub.modules.quest.application.dto;

import com.questhub.modules.quest.domain.personalquest.QuizAttempt;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record QuizAttemptResponse(
    UUID attemptId, BigDecimal score, BigDecimal maxScore, boolean passed, Instant createdAt) {

  public static QuizAttemptResponse from(QuizAttempt attempt) {
    return new QuizAttemptResponse(
        attempt.getId(),
        attempt.getScore(),
        attempt.getMaxScore(),
        attempt.isPassed(),
        attempt.getCreatedAt());
  }
}
