package com.questhub.modules.identity.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.questhub.modules.identity.application.request.LoginRequest;
import com.questhub.modules.identity.domain.user.Email;
import com.questhub.modules.identity.domain.user.User;
import com.questhub.modules.identity.domain.user.UserRepository;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private LoginUseCase useCase;

  @Test
  void login_whenCredentialsValid_shouldReturnUser() {
    User user = user("alice@example.com");
    when(userRepository.findByEmail(new Email("alice@example.com")))
        .thenReturn(Optional.of(user));
    when(passwordEncoder.matches("secret123", "$2a$10$hash")).thenReturn(true);

    User loggedIn = useCase.login(new LoginRequest("alice@example.com", "secret123"));

    assertThat(loggedIn).isSameAs(user);
    verify(passwordEncoder).matches("secret123", "$2a$10$hash");
  }

  @Test
  void login_whenEmailNotFound_shouldThrowUnauthorized() {
    when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.empty());

    BusinessException ex =
        catchThrowableOfType(
            () -> useCase.login(new LoginRequest("ghost@example.com", "x")),
            BusinessException.class);

    assertThat(ex.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(ex.getCode()).isEqualTo(ErrorCodes.INVALID_CREDENTIALS);
    verify(passwordEncoder, never()).matches(any(), any());
  }

  @Test
  void login_whenPasswordWrong_shouldThrowUnauthorized() {
    User user = user("alice@example.com");
    when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("sai", "$2a$10$hash")).thenReturn(false);

    BusinessException ex =
        catchThrowableOfType(
            () -> useCase.login(new LoginRequest("alice@example.com", "sai")),
            BusinessException.class);

    assertThat(ex.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(ex.getCode()).isEqualTo(ErrorCodes.INVALID_CREDENTIALS);
  }

  @Test
  void login_whenPasswordHashNull_shouldThrowUnauthorized() {
    User user =
        User.create(
            new Email("alice@example.com"),
            new com.questhub.modules.identity.domain.user.Username("alice"),
            new com.questhub.modules.identity.domain.user.DisplayName("Alice"),
            null);
    when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));

    BusinessException ex =
        catchThrowableOfType(
            () -> useCase.login(new LoginRequest("alice@example.com", "secret123")),
            BusinessException.class);

    assertThat(ex.getCode()).isEqualTo(ErrorCodes.INVALID_CREDENTIALS);
    verify(passwordEncoder, never()).matches(any(), any());
  }

  private User user(String email) {
    return User.restore(
        UUID.randomUUID(),
        new Email(email),
        new com.questhub.modules.identity.domain.user.Username("alice"),
        new com.questhub.modules.identity.domain.user.DisplayName("Alice"),
        "$2a$10$hash",
        com.questhub.modules.identity.domain.user.Role.USER,
        null,
        null,
        true,
        0,
        0,
        java.util.Map.of(),
        Instant.now(),
        Instant.now());
  }
}