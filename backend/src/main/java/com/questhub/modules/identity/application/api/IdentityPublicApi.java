package com.questhub.modules.identity.application.api;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Public contract của Identity bounded context.
 *
 * <p>Module khác CHỈ được phụ thuộc interface này, không import trực tiếp
 * query/usecase/entity bên trong identity (no entity sharing between contexts).
 */
public interface IdentityPublicApi {

  Optional<String> findUsername(UUID userId);

  Map<UUID, String> usernamesByIds(Collection<UUID> userIds);

  Optional<UUID> findPublicUserIdByUsername(String username);

  void promoteToCreator(UUID userId);
}
