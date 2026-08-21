package com.questhub.modules.identity.application.query;

import com.questhub.modules.identity.domain.user.User;
import com.questhub.modules.identity.domain.user.UserRepository;
import com.questhub.modules.identity.domain.user.Username;
import com.questhub.shared.annotation.UseCase;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class GetPublicUserIdQuery {

  private final UserRepository userRepository;

  public Optional<UUID> byUsername(String usernameValue) {
    return userRepository
        .findByUsername(new Username(usernameValue))
        .filter(User::isPublic)
        .map(User::getId);
  }
}
