package com.questhub.modules.quest.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record QuestAnalyticsDto(
    int forkCount,
    BigDecimal avgRating,
    int ratingCount,
    double completionRate,
    List<TaskDropOffStatDto> taskDropOff) {

  public record TaskDropOffStatDto(
      UUID sourceTaskId,
      long completedCount,
      long totalCount,
      double completionRate) {}
}
