package com.questhub.modules.world.infrastructure.persistence;

import com.questhub.modules.world.domain.Building;
import com.questhub.modules.world.domain.BuildingRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class BuildingRepositoryImpl implements BuildingRepository {

  private final SpringDataBuildingRepository jpa;

  public BuildingRepositoryImpl(SpringDataBuildingRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public Building save(Building building) {
    return BuildingMapper.toDomain(jpa.save(BuildingMapper.toEntity(building)));
  }

  @Override
  public List<Building> findByDistrictId(UUID districtId) {
    return jpa.findByDistrictIdOrderByPositionAsc(districtId).stream()
        .map(BuildingMapper::toDomain)
        .toList();
  }

  @Override
  public boolean existsByDistrictIdAndPosition(UUID districtId, int position) {
    return jpa.existsByDistrictIdAndPosition(districtId, position);
  }
}