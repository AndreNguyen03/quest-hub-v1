package com.questhub.modules.world.application.query;

import com.questhub.modules.identity.application.query.GetPublicUserIdQuery;
import com.questhub.modules.world.domain.district.District;
import com.questhub.modules.world.domain.district.DistrictRepository;
import com.questhub.modules.world.domain.world.World;
import com.questhub.modules.world.domain.world.WorldRepository;
import com.questhub.shared.annotation.UseCase;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class GetUserWorldQuery {

  private static final Logger log = LoggerFactory.getLogger(GetUserWorldQuery.class);

  private final GetPublicUserIdQuery getPublicUserIdQuery;
  private final WorldRepository worldRepository;
  private final DistrictRepository districtRepository;

  public record Result(World world, List<District> districts) {}

  @Transactional(
      readOnly = true,
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public Result getByUsername(String usernameValue) {
    UUID userId =
        getPublicUserIdQuery
            .byUsername(usernameValue)
            .orElseThrow(
                () ->
                    BusinessException.notFound(ErrorCodes.NOT_FOUND, "User not found: " + usernameValue));

    World world =
        worldRepository
            .findByUserId(userId)
            .orElseGet(() -> worldRepository.save(World.create(userId, usernameValue)));
    List<District> districts = districtRepository.findByWorldId(world.getId());
    log.info(
        "Public world viewed username={} userId={} worldId={} districts={}",
        usernameValue,
        userId,
        world.getId(),
        districts.size());
    return new Result(world, districts);
  }
}


