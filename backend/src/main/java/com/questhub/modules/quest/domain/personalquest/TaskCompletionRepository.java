package com.questhub.modules.quest.domain.personalquest;

import java.util.Map;
import java.util.UUID;

public interface TaskCompletionRepository {

  TaskCompletion save(TaskCompletion completion);

  void deleteByPersonalTaskId(UUID personalTaskId);

  long countByUserId(UUID userId);

  Map<UUID, Integer> countTasksPerDomain(UUID userId);
}