package com.questhub.modules.quest.infrastructure.persistence.learningpath;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataLearningPathRepository extends JpaRepository<LearningPathJpaEntity, UUID> {
  List<LearningPathJpaEntity> findByIsPublicTrueOrderByCreatedAtDesc();
}


