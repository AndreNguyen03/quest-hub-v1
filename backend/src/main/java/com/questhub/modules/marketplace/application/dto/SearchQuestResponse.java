package com.questhub.modules.marketplace.application.dto;

import com.questhub.modules.marketplace.infrastructure.elasticsearch.QuestDocument;
import java.math.BigDecimal;
import java.util.UUID;

public record SearchQuestResponse(
    UUID id,
    String title,
    String description,
    String difficulty,
    UUID learningPathId,
    UUID domainId,
    int forkCount,
    BigDecimal avgRating,
    int ratingCount) {

  public static SearchQuestResponse from(QuestDocument doc) {
    return new SearchQuestResponse(
        doc.getId(),
        doc.getTitle(),
        doc.getDescription(),
        doc.getDifficulty(),
        doc.getLearningPathId(),
        doc.getDomainId(),
        doc.getForkCount(),
        doc.getAvgRating(),
        doc.getRatingCount());
  }
}
