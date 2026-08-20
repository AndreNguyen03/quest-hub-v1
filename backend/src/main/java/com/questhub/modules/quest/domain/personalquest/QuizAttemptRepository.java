package com.questhub.modules.quest.domain.personalquest;

import java.util.List;
import java.util.UUID;

public interface QuizAttemptRepository {

  QuizAttempt save(QuizAttempt attempt);

  List<QuizAttempt> findByPersonalTaskIdOrderByCreatedAtDesc(UUID personalTaskId);
}