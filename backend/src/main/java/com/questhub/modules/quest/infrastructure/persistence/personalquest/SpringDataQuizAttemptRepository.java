package com.questhub.modules.quest.infrastructure.persistence.personalquest;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataQuizAttemptRepository extends JpaRepository<QuizAttemptEntity, UUID> {

  List<QuizAttemptEntity> findByPersonalTaskIdOrderByCreatedAtDesc(UUID personalTaskId);
}