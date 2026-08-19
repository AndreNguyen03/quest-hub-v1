package com.questhub.modules.identity.application.usecase;

import com.questhub.modules.identity.domain.user.User;
import com.questhub.modules.identity.domain.user.UserRepository;
import com.questhub.shared.annotation.UseCase;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class GetCurrentUserUseCase {

  private final UserRepository userRepository;

  public User getById(UUID userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(
            () -> BusinessException.notFound(ErrorCodes.NOT_FOUND, "User không tồn tại"));
  }
}