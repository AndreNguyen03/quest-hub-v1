package com.questhub.modules.marketplace.application.usecase;

import com.questhub.modules.marketplace.domain.favorite.Favorite;
import com.questhub.modules.marketplace.domain.favorite.FavoriteRepository;
import com.questhub.shared.annotation.UseCase;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class AddFavoriteUseCase {

  private final FavoriteRepository favoriteRepository;

  @Transactional(
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public Favorite add(UUID userId, UUID questId) {
    if (favoriteRepository.existsByUserIdAndQuestId(userId, questId)) {
      throw BusinessException.conflict(ErrorCodes.CONFLICT, "Bạn đã yêu thích quest này rồi");
    }
    return favoriteRepository.save(Favorite.create(userId, questId));
  }
}
