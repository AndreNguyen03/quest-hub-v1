package com.questhub.modules.quest.infrastructure.persistence.personalquest;

import com.questhub.modules.quest.domain.personalquest.TaskCompletion;
import com.questhub.modules.quest.domain.personalquest.TaskCompletionRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class TaskCompletionRepositoryImpl implements TaskCompletionRepository {

  private final SpringDataTaskCompletionRepository jpa;

  public TaskCompletionRepositoryImpl(SpringDataTaskCompletionRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public TaskCompletion save(TaskCompletion completion) {
    return TaskCompletionMapper.toDomain(jpa.save(TaskCompletionMapper.toEntity(completion)));
  }

  @Override
  public void deleteByPersonalTaskId(UUID personalTaskId) {
    jpa.deleteByPersonalTaskId(personalTaskId);
  }
}