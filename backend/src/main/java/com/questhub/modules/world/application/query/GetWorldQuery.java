package com.questhub.modules.world.application.query;

import com.questhub.modules.world.domain.district.District;
import com.questhub.modules.world.domain.district.DistrictRepository;
import com.questhub.modules.world.domain.world.World;
import com.questhub.modules.world.domain.world.WorldRepository;
import com.questhub.shared.annotation.UseCase;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class GetWorldQuery {

  private static final Logger log = LoggerFactory.getLogger(GetWorldQuery.class);

  private final WorldRepository worldRepository;
  private final DistrictRepository districtRepository;

  public record Result(World world, List<District> districts) {}

  @Transactional(
      readOnly = true,
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public Result get(UUID userId) {
    World world =
        worldRepository
            .findByUserId(userId)
            .orElseGet(() -> worldRepository.save(World.create(userId, null)));
    List<District> districts = districtRepository.findByWorldId(world.getId());
    log.info(
        "World viewed userId={} worldId={} districts={}", userId, world.getId(), districts.size());
    return new Result(world, districts);
  }
}

