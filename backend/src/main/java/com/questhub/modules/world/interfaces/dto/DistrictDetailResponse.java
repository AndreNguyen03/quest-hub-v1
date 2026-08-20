package com.questhub.modules.world.interfaces.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DistrictDetailResponse(
    UUID districtId,
    UUID domainId,
    String domainName,
    String domainSlug,
    int completionCount,
    int totalTasks,
    List<QuestRefResponse> quests,
    List<BuildingResponse> buildings) {

  public record QuestRefResponse(
      UUID personalQuestId, UUID questId, String title, String status, int progress) {}

  public record BuildingResponse(UUID id, String type, Instant unlockedAt, int position) {}
}