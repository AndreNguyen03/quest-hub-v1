package com.questhub.modules.quest.infrastructure.persistence.personalquest;

import com.questhub.modules.quest.domain.personalquest.TaskCompletion;

public final class TaskCompletionMapper {

  private TaskCompletionMapper() {}

  public static TaskCompletionEntity toEntity(TaskCompletion completion) {
    return new TaskCompletionEntity(
        completion.getId(),
        completion.getPersonalTaskId(),
        completion.getUserId(),
        completion.getEvidence(),
        completion.getCompletedAt(),
        completion.getCreatedAt());
  }

  public static TaskCompletion toDomain(TaskCompletionEntity entity) {
    return TaskCompletion.restore(
        entity.getId(),
        entity.getPersonalTaskId(),
        entity.getUserId(),
        entity.getEvidence(),
        entity.getCompletedAt(),
        entity.getCreatedAt());
  }
}