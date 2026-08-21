package com.questhub.modules.marketplace.application.query;

import com.questhub.modules.marketplace.domain.favorite.Favorite;
import com.questhub.modules.marketplace.domain.favorite.FavoriteRepository;
import com.questhub.shared.annotation.UseCase;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class GetUserFavoritesQuery {

  private final FavoriteRepository favoriteRepository;

  @Transactional(
      readOnly = true,
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public List<Favorite> get(UUID userId) {
    return favoriteRepository.findByUserId(userId);
  }
}
