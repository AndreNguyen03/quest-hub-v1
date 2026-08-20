package com.questhub.modules.world.infrastructure.persistence;

import com.questhub.modules.world.domain.DistrictEventRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class DistrictEventRepositoryImpl implements DistrictEventRepository {

  private final SpringDataDistrictEventRepository jpa;

  public DistrictEventRepositoryImpl(SpringDataDistrictEventRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public boolean existsByEventId(UUID eventId) {
    return jpa.existsByEventId(eventId);
  }

  @Override
  public void record(UUID eventId, UUID districtId, int delta) {
    jpa.save(new DistrictEventEntity(eventId, districtId, delta, Instant.now()));
  }
}