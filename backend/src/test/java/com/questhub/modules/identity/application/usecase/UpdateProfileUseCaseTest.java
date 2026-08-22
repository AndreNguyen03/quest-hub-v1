package com.questhub.modules.identity.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.questhub.modules.identity.application.command.UpdateProfileCommand;
import com.questhub.modules.identity.domain.user.Email;
import com.questhub.modules.identity.domain.user.Role;
import com.questhub.modules.identity.domain.user.User;
import com.questhub.modules.identity.domain.user.UserRepository;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.questhub.shared.domain.ResponseStatus;

@ExtendWith(MockitoExtension.class)
class UpdateProfileUseCaseTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private UpdateProfileUseCase useCase;

  @Test
  void update_whenUserExists_shouldApplyAndSaveProfileChanges() {
    UUID id = UUID.randomUUID();
    User user = user(id);
    when(userRepository.findById(id)).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    User updated =
        useCase.update(
            id,
            new UpdateProfileCommand("http://x.com/a.png", "Dev", "Alice B", false));

    assertThat(updated.getAvatarUrl()).isEqualTo("http://x.com/a.png");
    assertThat(updated.getBio()).isEqualTo("Dev");
    assertThat(updated.getDisplayName().value()).isEqualTo("Alice B");
    assertThat(updated.isPublic()).isFalse();
    verify(userRepository).save(user);
  }

  @Test
  void update_whenUserNotFound_shouldThrowNotFound() {
    UUID id = UUID.randomUUID();
    when(userRepository.findById(id)).thenReturn(Optional.empty());

    BusinessException ex =
        catchThrowableOfType(
            () -> useCase.update(id, new UpdateProfileCommand(null, null, "Alice", true)),
            BusinessException.class);

    assertThat(ex.getStatus()).isEqualTo(ResponseStatus.NOT_FOUND);
    assertThat(ex.getCode()).isEqualTo(ErrorCodes.NOT_FOUND);
    verify(userRepository, never()).save(any());
  }

  private User user(UUID id) {
    return User.restore(
        id,
        new Email("alice@example.com"),
        new com.questhub.modules.identity.domain.user.Username("alice"),
        new com.questhub.modules.identity.domain.user.DisplayName("Alice"),
        "$2a$10$hash",
        Role.USER,
        null,
        null,
        true,
        0,
        0,
        Map.of(),
        Instant.now(),
        Instant.now());
  }
}

