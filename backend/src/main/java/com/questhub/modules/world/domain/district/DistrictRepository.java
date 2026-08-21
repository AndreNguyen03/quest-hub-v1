package com.questhub.modules.world.domain.district;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DistrictRepository {

  District save(District district);

  Optional<District> findById(UUID id);

  Optional<District> findByWorldIdAndDomainId(UUID worldId, UUID domainId);

  List<District> findByWorldId(UUID worldId);
}


