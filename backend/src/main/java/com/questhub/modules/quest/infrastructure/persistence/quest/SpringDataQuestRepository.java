package com.questhub.modules.quest.infrastructure.persistence.quest;

import com.questhub.modules.quest.domain.quest.QuestVisibility;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataQuestRepository extends JpaRepository<QuestJpaEntity, UUID> {

  boolean existsByCreatorIdAndVisibility(UUID creatorId, QuestVisibility visibility);

  @Query("select distinct q from QuestJpaEntity q join q.chapters c join c.tasks t where t.id = :taskId")
  Optional<QuestJpaEntity> findByTaskId(@Param("taskId") UUID taskId);

  @Query(
      "select distinct q from QuestJpaEntity q join q.chapters c join c.tasks t join t.resources r where r.id = :resourceId")
  Optional<QuestJpaEntity> findByResourceId(@Param("resourceId") UUID resourceId);

  @Modifying
  @Query("update QuestJpaEntity q set q.forkCount = q.forkCount + 1 where q.id = :questId")
  void incrementForkCount(@Param("questId") UUID questId);

  @Query(
      "select q from QuestJpaEntity q where q.visibility = :visibility order by q.forkCount desc, q.avgRating desc")
  List<QuestJpaEntity> findPopular(
      @Param("visibility") QuestVisibility visibility, Pageable pageable);

  @Query(
      "select q from QuestJpaEntity q where q.visibility = :visibility and q.publishedAt > :since order by q.forkCount desc, q.publishedAt desc")
  List<QuestJpaEntity> findTrending(
      @Param("visibility") QuestVisibility visibility, @Param("since") Instant since, Pageable pageable);
}



