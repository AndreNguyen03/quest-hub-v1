package com.questhub.modules.marketplace.application.query;

import com.questhub.modules.quest.application.dto.LearningPathDto;

public final class MarketplaceHomeMapper {

  private MarketplaceHomeMapper() {}

  public static MarketplaceHomeQuery.PathCard toPathCard(LearningPathDto dto) {
    return new MarketplaceHomeQuery.PathCard(
        dto.id(),
        dto.title(),
        dto.description(),
        dto.difficulty(),
        dto.domainId());
  }
}





