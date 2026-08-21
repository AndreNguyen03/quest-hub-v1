package com.questhub.modules.quest.infrastructure.persistence.personalquest;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataTaskCompletionRepository
    extends JpaRepository<TaskCompletionJpaEntity, UUID> {

  void deleteByPersonalTaskId(UUID personalTaskId);

  long countByUserId(UUID userId);

  @Query(
      value =
          "SELECT lp.domain_id as domainId, COUNT(*) as cnt "
              + "FROM task_completions tc "
              + "JOIN personal_tasks pt ON pt.id = tc.personal_task_id "
              + "JOIN personal_chapters pc ON pc.id = pt.personal_chapter_id "
              + "JOIN personal_quests pq ON pq.id = pc.personal_quest_id "
              + "LEFT JOIN learning_paths lp ON lp.id = pq.learning_path_id "
              + "WHERE tc.user_id = :userId AND lp.domain_id IS NOT NULL "
              + "GROUP BY lp.domain_id",
      nativeQuery = true)
  List<Object[]> countTasksPerDomainRaw(@Param("userId") UUID userId);
}




