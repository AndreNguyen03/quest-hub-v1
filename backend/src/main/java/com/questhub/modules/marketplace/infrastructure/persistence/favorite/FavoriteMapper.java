package com.questhub.modules.marketplace.infrastructure.persistence.favorite;

import com.questhub.modules.marketplace.domain.favorite.Favorite;

public final class FavoriteMapper {

  private FavoriteMapper() {}

  public static FavoriteJpaEntity toEntity(Favorite favorite) {
    return new FavoriteJpaEntity(
        new FavoriteId(favorite.getUserId(), favorite.getQuestId()), favorite.getCreatedAt());
  }

  public static Favorite toDomain(FavoriteJpaEntity entity) {
    return Favorite.restore(
        entity.getId().getUserId(), entity.getId().getQuestId(), entity.getCreatedAt());
  }
}
