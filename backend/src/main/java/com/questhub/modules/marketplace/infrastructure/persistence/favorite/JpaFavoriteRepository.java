package com.questhub.modules.marketplace.infrastructure.persistence.favorite;

import com.questhub.modules.marketplace.domain.favorite.Favorite;
import com.questhub.modules.marketplace.domain.favorite.FavoriteRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JpaFavoriteRepository implements FavoriteRepository {

  private final SpringDataFavoriteRepository jpa;

  public JpaFavoriteRepository(SpringDataFavoriteRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public Favorite save(Favorite favorite) {
    FavoriteJpaEntity entity =
        new FavoriteJpaEntity(
            new FavoriteId(favorite.getUserId(), favorite.getQuestId()),
            favorite.getCreatedAt());
    jpa.save(entity);
    return favorite;
  }

  @Override
  public void deleteByUserIdAndQuestId(UUID userId, UUID questId) {
    jpa.deleteByIdUserIdAndIdQuestId(userId, questId);
  }

  @Override
  public List<Favorite> findByUserId(UUID userId) {
    return jpa.findByIdUserIdOrderByCreatedAtDesc(userId).stream()
        .map(
            entity ->
                Favorite.restore(
                    entity.getId().getUserId(),
                    entity.getId().getQuestId(),
                    entity.getCreatedAt()))
        .toList();
  }

  @Override
  public boolean existsByUserIdAndQuestId(UUID userId, UUID questId) {
    return jpa.existsByIdUserIdAndIdQuestId(userId, questId);
  }
}
