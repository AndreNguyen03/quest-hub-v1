package com.questhub.modules.identity.application.query;

import com.questhub.modules.identity.domain.user.User;
import com.questhub.modules.identity.domain.user.UserRepository;
import com.questhub.shared.annotation.UseCase;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class GetUsernameQuery {

  private final UserRepository userRepository;

  public Optional<String> byUserId(UUID userId) {
    return userRepository.findById(userId).map(user -> user.getUsername().value());
  }

  public Map<UUID, String> byIds(Collection<UUID> userIds) {
    return userRepository.findAllById(userIds).stream()
        .collect(Collectors.toMap(User::getId, user -> user.getUsername().value()));
  }
}
