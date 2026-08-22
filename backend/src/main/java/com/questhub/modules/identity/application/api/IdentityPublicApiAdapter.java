package com.questhub.modules.identity.application.api;

import com.questhub.modules.identity.application.query.GetPublicUserIdQuery;
import com.questhub.modules.identity.application.query.GetUsernameQuery;
import com.questhub.modules.identity.application.usecase.PromoteToCreatorUseCase;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Adapter expose internal queries/usecase của Identity qua contract IdentityPublicApi. */
@Component
@RequiredArgsConstructor
public class IdentityPublicApiAdapter implements IdentityPublicApi {

  private final GetUsernameQuery getUsernameQuery;
  private final GetPublicUserIdQuery getPublicUserIdQuery;
  private final PromoteToCreatorUseCase promoteToCreatorUseCase;

  @Override
  public Optional<String> findUsername(UUID userId) {
    return getUsernameQuery.byUserId(userId);
  }

  @Override
  public Map<UUID, String> usernamesByIds(Collection<UUID> userIds) {
    return getUsernameQuery.byIds(userIds);
  }

  @Override
  public Optional<UUID> findPublicUserIdByUsername(String username) {
    return getPublicUserIdQuery.byUsername(username);
  }

  @Override
  public void promoteToCreator(UUID userId) {
    promoteToCreatorUseCase.promote(userId);
  }
}
