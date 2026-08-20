package com.questhub.modules.quest.domain.learningpath;

import java.util.Optional;
import java.util.UUID;

public interface LearningPathRepository {

  LearningPath save(LearningPath learningPath);

  Optional<LearningPath> findById(UUID id);

  boolean existsById(UUID id);
}