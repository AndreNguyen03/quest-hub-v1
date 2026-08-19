package com.questhub.modules.identity.application.usecase;

import com.questhub.modules.identity.application.request.UpdateProfileRequest;
import com.questhub.modules.identity.domain.user.DisplayName;
import com.questhub.modules.identity.domain.user.User;
import com.questhub.modules.identity.domain.user.UserRepository;
import com.questhub.shared.annotation.UseCase;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class UpdateProfileUseCase {

  private final UserRepository userRepository;

  public User update(UUID userId, UpdateProfileRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> BusinessException.notFound(ErrorCodes.NOT_FOUND, "User không tồn tại"));

    user.updateProfile(
        request.avatarUrl(),
        request.bio(),
        new DisplayName(request.displayName()),
        request.isPublic());

    return userRepository.save(user);
  }
}