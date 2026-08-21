package com.questhub.modules.marketplace.infrastructure.persistence.favorite;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
public class FavoriteId implements Serializable {

  private UUID userId;
  private UUID questId;

  protected FavoriteId() {}

  public FavoriteId(UUID userId, UUID questId) {
    this.userId = userId;
    this.questId = questId;
  }

  public UUID getUserId() {
    return userId;
  }

  public UUID getQuestId() {
    return questId;
  }
}
