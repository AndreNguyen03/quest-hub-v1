package com.questhub.modules.quest.interfaces.dto;

import com.questhub.modules.quest.domain.learningpath.LearningPath;
import com.questhub.modules.quest.domain.quest.Difficulty;
import java.time.Instant;
import java.util.UUID;

public record LearningPathResponse(
    UUID id,
    UUID authorId,
    UUID domainId,
    String title,
    String description,
    Difficulty difficulty,
    int estimatedDuration,
    boolean isPublic,
    Instant createdAt,
    Instant updatedAt) {

  public static LearningPathResponse from(LearningPath path) {
    return new LearningPathResponse(
        path.getId(),
        path.getAuthorId(),
        path.getDomainId(),
        path.getTitle(),
        path.getDescription(),
        path.getDifficulty(),
        path.getEstimatedDuration(),
        path.isPublic(),
        path.getCreatedAt(),
        path.getUpdatedAt());
  }
}