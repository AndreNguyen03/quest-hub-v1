package com.questhub.modules.quest.infrastructure.persistence.personalquest;

import com.questhub.modules.quest.domain.personalquest.PersonalQuestStatus;
import com.questhub.modules.quest.infrastructure.persistence.LearningPathEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataPersonalQuestRepository extends JpaRepository<PersonalQuestEntity, UUID> {

  boolean existsByUserIdAndQuestId(UUID userId, UUID questId);

  Optional<PersonalQuestEntity> findByIdAndUserId(UUID id, UUID userId);

  List<PersonalQuestEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);

  @Query(
      "SELECT pq FROM PersonalQuestEntity pq JOIN LearningPathEntity lp ON lp.id = pq.learningPathId "
          + "WHERE pq.userId = :userId AND lp.domainId = :domainId AND pq.status IN :statuses "
          + "ORDER BY pq.createdAt DESC")
  List<PersonalQuestEntity> findByUserIdAndDomainIdAndStatusIn(
      @Param("userId") UUID userId,
      @Param("domainId") UUID domainId,
      @Param("statuses") Collection<PersonalQuestStatus> statuses);
}