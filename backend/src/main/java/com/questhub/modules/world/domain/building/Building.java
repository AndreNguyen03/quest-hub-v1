package com.questhub.modules.world.domain.building;

import java.time.Instant;
import java.util.UUID;

public class Building {

  private final UUID id;
  private final UUID districtId;
  private final String type;
  private final Instant unlockedAt;
  private final int position;

  private Building(UUID id, UUID districtId, String type, Instant unlockedAt, int position) {
    this.id = id;
    this.districtId = districtId;
    this.type = type;
    this.unlockedAt = unlockedAt;
    this.position = position;
  }

  public static Building create(UUID districtId, String type, int position) {
    return new Building(UUID.randomUUID(), districtId, type, Instant.now(), position);
  }

  public static Building restore(
      UUID id, UUID districtId, String type, Instant unlockedAt, int position) {
    return new Building(id, districtId, type, unlockedAt, position);
  }

  public UUID getId() {
    return id;
  }

  public UUID getDistrictId() {
    return districtId;
  }

  public String getType() {
    return type;
  }

  public Instant getUnlockedAt() {
    return unlockedAt;
  }

  public int getPosition() {
    return position;
  }
}


