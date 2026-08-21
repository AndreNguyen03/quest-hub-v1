package com.questhub.modules.world.infrastructure.persistence.district;

import com.questhub.modules.world.domain.district.DistrictEventRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JpaDistrictEventRepository implements DistrictEventRepository {

  private final SpringDataDistrictEventRepository jpa;

  public JpaDistrictEventRepository(SpringDataDistrictEventRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public boolean existsByEventId(UUID eventId) {
    return jpa.existsByEventId(eventId);
  }

  @Override
  public void record(UUID eventId, UUID districtId, int delta) {
    jpa.save(new DistrictEventJpaEntity(eventId, districtId, delta, Instant.now()));
  }
}





