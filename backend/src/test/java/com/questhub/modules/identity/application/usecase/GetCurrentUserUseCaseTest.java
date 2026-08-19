package com.questhub.modules.identity.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.when;

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
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class GetCurrentUserUseCaseTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private GetCurrentUserUseCase useCase;

  @Test
  void getById_whenUserExists_shouldReturnUser() {
    UUID id = UUID.randomUUID();
    User user = user(id);
    when(userRepository.findById(id)).thenReturn(Optional.of(user));

    User result = useCase.getById(id);

    assertThat(result).isSameAs(user);
  }

  @Test
  void getById_whenUserMissing_shouldThrowNotFound() {
    UUID id = UUID.randomUUID();
    when(userRepository.findById(id)).thenReturn(Optional.empty());

    BusinessException ex = catchThrowableOfType(() -> useCase.getById(id), BusinessException.class);

    assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(ex.getCode()).isEqualTo(ErrorCodes.NOT_FOUND);
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