package com.questhub.modules.world.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "buildings")
public class BuildingEntity {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "district_id", nullable = false)
  private UUID districtId;

  @Column(name = "type", nullable = false, length = 50)
  private String type;

  @Column(name = "unlocked_at", nullable = false)
  private Instant unlockedAt;

  @Column(name = "position", nullable = false)
  private int position;

  protected BuildingEntity() {}

  public BuildingEntity(
      UUID id, UUID districtId, String type, Instant unlockedAt, int position) {
    this.id = id;
    this.districtId = districtId;
    this.type = type;
    this.unlockedAt = unlockedAt;
    this.position = position;
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