package com.questhub.modules.world.domain.district;

import java.util.UUID;

public interface DistrictEventRepository {

  boolean existsByEventId(UUID eventId);

  void record(UUID eventId, UUID districtId, int delta);
}


