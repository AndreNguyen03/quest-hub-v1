package com.questhub.modules.world.infrastructure.persistence.district;

import com.questhub.modules.world.domain.district.District;
import com.questhub.modules.world.domain.district.DistrictRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JpaDistrictRepository implements DistrictRepository {

  private final SpringDataDistrictRepository jpa;

  public JpaDistrictRepository(SpringDataDistrictRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public District save(District district) {
    return DistrictMapper.toDomain(jpa.save(DistrictMapper.toEntity(district)));
  }

  @Override
  public Optional<District> findById(UUID id) {
    return jpa.findById(id).map(DistrictMapper::toDomain);
  }

  @Override
  public Optional<District> findByWorldIdAndDomainId(UUID worldId, UUID domainId) {
    return jpa.findByWorldIdAndDomainId(worldId, domainId).map(DistrictMapper::toDomain);
  }

  @Override
  public List<District> findByWorldId(UUID worldId) {
    return jpa.findByWorldIdOrderByCreatedAtAsc(worldId).stream()
        .map(DistrictMapper::toDomain)
        .toList();
  }
}




