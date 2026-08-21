package com.questhub.modules.identity.application.query;

import com.questhub.modules.identity.domain.user.User;
import com.questhub.modules.identity.domain.user.UserRepository;
import com.questhub.shared.annotation.UseCase;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class GetUserProfileQuery {

  private final UserRepository userRepository;

  public User getById(UUID userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(
            () -> BusinessException.notFound(ErrorCodes.NOT_FOUND, "User không tồn tại"));
  }
}
