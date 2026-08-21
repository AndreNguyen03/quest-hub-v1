package com.questhub.modules.marketplace.application.dto;

import com.questhub.modules.quest.application.dto.QuestDto;
import java.math.BigDecimal;
import java.util.UUID;

public record QuestCardResponse(
    UUID id,
    String title,
    String description,
    String difficulty,
    UUID domainId,
    int forkCount,
    Double avgRating) {

  public static QuestCardResponse from(QuestDto dto) {
    return new QuestCardResponse(
        dto.id(),
        dto.title(),
        dto.description(),
        dto.difficulty(),
        null,
        dto.forkCount(),
        dto.avgRating() != null ? dto.avgRating().doubleValue() : null);
  }
}
