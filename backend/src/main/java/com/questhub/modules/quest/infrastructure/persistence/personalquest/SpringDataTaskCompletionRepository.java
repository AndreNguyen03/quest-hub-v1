package com.questhub.modules.quest.infrastructure.persistence.personalquest;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataTaskCompletionRepository
    extends JpaRepository<TaskCompletionEntity, UUID> {

  void deleteByPersonalTaskId(UUID personalTaskId);
}