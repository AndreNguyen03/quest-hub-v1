package com.questhub.modules.marketplace.application.dto;

import com.questhub.modules.marketplace.domain.favorite.Favorite;
import java.time.Instant;
import java.util.UUID;

public record FavoriteResponse(
    UUID questId,
    UUID userId,
    Instant createdAt) {

  public static FavoriteResponse from(Favorite favorite) {
    return new FavoriteResponse(
        favorite.getQuestId(), favorite.getUserId(), favorite.getCreatedAt());
  }
}
