package com.questhub.modules.marketplace.infrastructure.persistence.favorite;

import com.questhub.modules.marketplace.domain.favorite.Favorite;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "favorites")
public class FavoriteJpaEntity {

  @EmbeddedId
  private FavoriteId id;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected FavoriteJpaEntity() {}

  public FavoriteJpaEntity(FavoriteId id, Instant createdAt) {
    this.id = id;
    this.createdAt = createdAt;
  }

  public FavoriteId getId() {
    return id;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
