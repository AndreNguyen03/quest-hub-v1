package com.questhub.modules.quest.infrastructure.persistence;

import com.questhub.modules.quest.domain.quest.QuestVisibility;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataQuestRepository extends JpaRepository<QuestEntity, UUID> {

  boolean existsByCreatorIdAndVisibility(UUID creatorId, QuestVisibility visibility);

  @Query("select distinct q from QuestEntity q join q.chapters c join c.tasks t where t.id = :taskId")
  Optional<QuestEntity> findByTaskId(@Param("taskId") UUID taskId);

  @Query(
      "select distinct q from QuestEntity q join q.chapters c join c.tasks t join t.resources r where r.id = :resourceId")
  Optional<QuestEntity> findByResourceId(@Param("resourceId") UUID resourceId);

  @Modifying
  @Query("update QuestEntity q set q.forkCount = q.forkCount + 1 where q.id = :questId")
  void incrementForkCount(@Param("questId") UUID questId);
}