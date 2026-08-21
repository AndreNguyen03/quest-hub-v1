package com.questhub.modules.quest.infrastructure.persistence.personalquest;

import com.questhub.modules.quest.domain.personalquest.TaskCompletion;
import com.questhub.modules.quest.domain.personalquest.TaskCompletionRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JpaTaskCompletionRepository implements TaskCompletionRepository {

  private final SpringDataTaskCompletionRepository jpa;

  public JpaTaskCompletionRepository(SpringDataTaskCompletionRepository jpa) {
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

  @Override
  public long countByUserId(UUID userId) {
    return jpa.countByUserId(userId);
  }

  @Override
  public Map<UUID, Integer> countTasksPerDomain(UUID userId) {
    List<Object[]> rows = jpa.countTasksPerDomainRaw(userId);
    Map<UUID, Integer> result = new HashMap<>();
    for (Object[] row : rows) {
      UUID domainId = (UUID) row[0];
      Number cnt = (Number) row[1];
      result.put(domainId, cnt.intValue());
    }
    return result;
  }
}




