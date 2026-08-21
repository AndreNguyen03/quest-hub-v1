package com.questhub.modules.quest.domain.learningpath;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LearningPathRepository {

  LearningPath save(LearningPath learningPath);

  List<LearningPath> findByIsPublicTrueOrderByCreatedAtDesc();

  Optional<LearningPath> findById(UUID id);

  boolean existsById(UUID id);
}