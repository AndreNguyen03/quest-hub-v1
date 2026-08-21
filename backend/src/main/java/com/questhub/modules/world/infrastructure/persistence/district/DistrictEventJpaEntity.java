package com.questhub.modules.world.infrastructure.persistence.district;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "district_events")
public class DistrictEventJpaEntity {

  @Id
  @Column(name = "event_id")
  private UUID eventId;

  @Column(name = "district_id", nullable = false)
  private UUID districtId;

  @Column(name = "delta", nullable = false)
  private int delta;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected DistrictEventJpaEntity() {}

  public DistrictEventJpaEntity(UUID eventId, UUID districtId, int delta, Instant createdAt) {
    this.eventId = eventId;
    this.districtId = districtId;
    this.delta = delta;
    this.createdAt = createdAt;
  }
}



