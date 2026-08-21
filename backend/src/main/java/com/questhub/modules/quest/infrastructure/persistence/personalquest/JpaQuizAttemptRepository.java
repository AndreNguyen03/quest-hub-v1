package com.questhub.modules.quest.infrastructure.persistence.personalquest;

import com.questhub.modules.quest.domain.personalquest.QuizAttempt;
import com.questhub.modules.quest.domain.personalquest.QuizAttemptRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JpaQuizAttemptRepository implements QuizAttemptRepository {

  private final SpringDataQuizAttemptRepository jpa;

  public JpaQuizAttemptRepository(SpringDataQuizAttemptRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public QuizAttempt save(QuizAttempt attempt) {
    return QuizAttemptMapper.toDomain(jpa.save(QuizAttemptMapper.toEntity(attempt)));
  }

  @Override
  public List<QuizAttempt> findByPersonalTaskIdOrderByCreatedAtDesc(UUID personalTaskId) {
    return jpa.findByPersonalTaskIdOrderByCreatedAtDesc(personalTaskId).stream()
        .map(QuizAttemptMapper::toDomain)
        .toList();
  }
}




