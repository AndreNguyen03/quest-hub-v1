package com.questhub.modules.identity.application.usecase;

import com.questhub.modules.identity.domain.user.Role;
import com.questhub.modules.identity.domain.user.UserRepository;
import com.questhub.shared.annotation.UseCase;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class PromoteToCreatorUseCase {

  private final UserRepository userRepository;

  @Transactional(
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public void promote(UUID userId) {
    userRepository
        .findById(userId)
        .ifPresent(
            user -> {
              if (user.getRole() == Role.USER) {
                user.promoteToCreator();
                userRepository.save(user);
                log.info("User promoted to CREATOR userId={}", userId);
              }
            });
  }
}
