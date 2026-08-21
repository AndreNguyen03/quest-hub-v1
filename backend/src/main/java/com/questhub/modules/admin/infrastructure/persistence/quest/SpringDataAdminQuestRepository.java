package com.questhub.modules.admin.infrastructure.persistence.quest;

import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SpringDataAdminQuestRepository extends JpaRepository<AdminQuestJpaEntity, UUID> {

  @Query("SELECT q FROM AdminQuestJpaEntity q ORDER BY q.createdAt DESC")
  java.util.List<AdminQuestJpaEntity> findAllQuests(org.springframework.data.domain.Pageable pageable);

  @Modifying
  @Query(value = "UPDATE quests SET visibility = :visibility, updated_at = NOW() WHERE id = :id", nativeQuery = true)
  int updateVisibility(UUID id, String visibility);
}
