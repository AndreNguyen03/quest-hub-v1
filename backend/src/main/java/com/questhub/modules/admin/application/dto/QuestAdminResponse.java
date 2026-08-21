package com.questhub.modules.admin.application.dto;

import com.questhub.modules.admin.infrastructure.persistence.quest.AdminQuestJpaEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record QuestAdminResponse(
    UUID id,
    UUID creatorId,
    String title,
    String visibility,
    int forkCount,
    BigDecimal avgRating,
    Instant createdAt,
    Instant updatedAt) {

  public static QuestAdminResponse from(Map<String, Object> row) {
    return new QuestAdminResponse(
        UUID.fromString((String) row.get("id")),
        UUID.fromString((String) row.get("creator_id")),
        (String) row.get("title"),
        (String) row.get("visibility"),
        ((Number) row.get("fork_count")).intValue(),
        row.get("avg_rating") != null ? new BigDecimal(row.get("avg_rating").toString()) : null,
        ((java.sql.Timestamp) row.get("created_at")).toInstant(),
        ((java.sql.Timestamp) row.get("updated_at")).toInstant());
  }

  public static QuestAdminResponse from(AdminQuestJpaEntity entity) {
    return new QuestAdminResponse(
        entity.getId(),
        entity.getCreatorId(),
        entity.getTitle(),
        entity.getVisibility(),
        entity.getForkCount(),
        entity.getAvgRating(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
