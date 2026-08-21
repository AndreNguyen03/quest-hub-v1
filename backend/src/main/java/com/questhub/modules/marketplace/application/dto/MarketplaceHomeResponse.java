package com.questhub.modules.marketplace.application.dto;

import java.util.List;
import java.util.UUID;

public record MarketplaceHomeResponse(
    List<DomainGroup> pathsByDomain,
    List<QuestCard> popular,
    List<QuestCard> trending) {

  public record DomainGroup(UUID domainId, String domainName, String slug, List<PathCard> paths) {}
  public record PathCard(UUID id, String title, String description, String difficulty, UUID domainId) {}
  public record QuestCard(UUID id, String title, String description, String difficulty, UUID domainId, int forkCount, Double avgRating) {}
}



