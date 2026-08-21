package com.questhub.modules.marketplace.application.dto;

import com.questhub.modules.marketplace.application.query.MarketplaceHomeQuery;

public final class MarketplaceHomeResponseMapper {

  private MarketplaceHomeResponseMapper() {}

  public static MarketplaceHomeResponse toResponse(MarketplaceHomeQuery.HomeResult result) {
    return new MarketplaceHomeResponse(
        result.pathsByDomain().stream().map(MarketplaceHomeResponseMapper::toDomainGroup).toList(),
        result.popular().stream().map(MarketplaceHomeResponseMapper::toQuestCard).toList(),
        result.trending().stream().map(MarketplaceHomeResponseMapper::toQuestCard).toList());
  }

  private static MarketplaceHomeResponse.DomainGroup toDomainGroup(
      MarketplaceHomeQuery.DomainGroup group) {
    return new MarketplaceHomeResponse.DomainGroup(
        group.domainId(),
        group.domainName(),
        group.slug(),
        group.paths().stream().map(MarketplaceHomeResponseMapper::toPathCard).toList());
  }

  private static MarketplaceHomeResponse.PathCard toPathCard(MarketplaceHomeQuery.PathCard card) {
    return new MarketplaceHomeResponse.PathCard(
        card.id(), card.title(), card.description(), card.difficulty(), card.domainId());
  }

  private static MarketplaceHomeResponse.QuestCard toQuestCard(MarketplaceHomeQuery.QuestCard card) {
    return new MarketplaceHomeResponse.QuestCard(
        card.id(), card.title(), card.description(), card.difficulty(), card.domainId(), card.forkCount(), card.avgRating());
  }
}





