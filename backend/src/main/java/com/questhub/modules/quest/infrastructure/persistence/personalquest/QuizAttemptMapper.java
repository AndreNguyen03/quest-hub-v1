package com.questhub.modules.quest.infrastructure.persistence.personalquest;

import com.questhub.modules.quest.domain.personalquest.QuizAttempt;

public final class QuizAttemptMapper {

  private QuizAttemptMapper() {}

  public static QuizAttemptJpaEntity toEntity(QuizAttempt attempt) {
    return new QuizAttemptJpaEntity(
        attempt.getId(),
        attempt.getPersonalTaskId(),
        attempt.getUserId(),
        attempt.getScore(),
        attempt.getMaxScore(),
        attempt.isPassed(),
        attempt.getAnswers(),
        attempt.getCreatedAt());
  }

  public static QuizAttempt toDomain(QuizAttemptJpaEntity entity) {
    return QuizAttempt.restore(
        entity.getId(),
        entity.getPersonalTaskId(),
        entity.getUserId(),
        entity.getScore(),
        entity.getMaxScore(),
        entity.isPassed(),
        entity.getAnswers(),
        entity.getCreatedAt());
  }
}




