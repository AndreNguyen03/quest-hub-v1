package com.questhub.modules.world.domain.building;

import java.util.List;
import java.util.UUID;

public interface BuildingRepository {

  Building save(Building building);

  List<Building> findByDistrictId(UUID districtId);

  boolean existsByDistrictIdAndPosition(UUID districtId, int position);
}


