package com.questhub.modules.quest.infrastructure.persistence.learningpath;

import com.questhub.modules.quest.domain.learningpath.LearningPath;
import com.questhub.modules.quest.domain.learningpath.LearningPathRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JpaLearningPathRepository implements LearningPathRepository {

  private final SpringDataLearningPathRepository jpa;

  public JpaLearningPathRepository(SpringDataLearningPathRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public LearningPath save(LearningPath learningPath) {
    return LearningPathMapper.toDomain(jpa.save(LearningPathMapper.toEntity(learningPath)));
  }

  @Override
  public Optional<LearningPath> findById(UUID id) {
    return jpa.findById(id).map(LearningPathMapper::toDomain);
  }

  @Override
  public boolean existsById(UUID id) {
    return jpa.existsById(id);
  }

  @Override
  public List<LearningPath> findByIsPublicTrueOrderByCreatedAtDesc() {
    return jpa.findByIsPublicTrueOrderByCreatedAtDesc().stream()
        .map(LearningPathMapper::toDomain)
        .toList();
  }
}


