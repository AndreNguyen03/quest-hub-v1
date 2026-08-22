package com.questhub.modules.quest.infrastructure.persistence.personalquest;

import com.questhub.modules.quest.domain.personalquest.PersonalQuestStatus;
import com.questhub.modules.quest.infrastructure.persistence.learningpath.LearningPathJpaEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataPersonalQuestRepository extends JpaRepository<PersonalQuestJpaEntity, UUID> {

  boolean existsByUserIdAndQuestId(UUID userId, UUID questId);

  Optional<PersonalQuestJpaEntity> findByIdAndUserId(UUID id, UUID userId);

  List<PersonalQuestJpaEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);

  long countByUserIdAndStatus(UUID userId, PersonalQuestStatus status);

  @Query(
      "SELECT pq FROM PersonalQuestJpaEntity pq JOIN LearningPathJpaEntity lp ON lp.id = pq.learningPathId "
          + "WHERE pq.userId = :userId AND lp.domainId = :domainId AND pq.status IN :statuses "
          + "ORDER BY pq.createdAt DESC")
  List<PersonalQuestJpaEntity> findByUserIdAndDomainIdAndStatusIn(
      @Param("userId") UUID userId,
      @Param("domainId") UUID domainId,
      @Param("statuses") Collection<PersonalQuestStatus> statuses);

  @Query(
      value =
          "SELECT q.user_id as userId, q.questCount as questCount, COALESCE(t.taskCount, 0) as taskCount "
              + "FROM (SELECT user_id, COUNT(*) as questCount FROM personal_quests "
              + "      WHERE status = 'COMPLETED' GROUP BY user_id) q "
              + "LEFT JOIN (SELECT user_id, COUNT(*) as taskCount FROM task_completions "
              + "           GROUP BY user_id) t ON t.user_id = q.user_id "
              + "ORDER BY q.questCount DESC, taskCount DESC LIMIT :limit",
      nativeQuery = true)
  List<LeaderboardStatRow> findTopCompletionStats(@Param("limit") int limit);

  interface LeaderboardStatRow {
    UUID getUserId();
    long getQuestCount();
    long getTaskCount();
  }

  @Query(
      value =
          "SELECT COALESCE(COUNT(*) FILTER (WHERE status = 'COMPLETED') * 100.0 / NULLIF(COUNT(*), 0), 0.0) "
              + "FROM personal_quests WHERE quest_id = :questId",
      nativeQuery = true)
  double findCompletionRateByQuestId(@Param("questId") UUID questId);

  @Query(
      value =
          "SELECT pt.source_task_id as sourceTaskId, "
              + "COUNT(*) FILTER (WHERE pt.is_completed = true) as completedCount, "
              + "COUNT(*) as totalCount "
              + "FROM personal_tasks pt "
              + "JOIN personal_quests pq ON pq.id = pt.personal_quest_id "
              + "WHERE pq.quest_id = :questId AND pt.source_task_id IS NOT NULL "
              + "GROUP BY pt.source_task_id "
              + "ORDER BY completedCount ASC",
      nativeQuery = true)
  List<TaskDropOffRow> findTaskDropOffByQuestId(@Param("questId") UUID questId);

  interface TaskDropOffRow {
    UUID getSourceTaskId();
    long getCompletedCount();
    long getTotalCount();
  }
}




