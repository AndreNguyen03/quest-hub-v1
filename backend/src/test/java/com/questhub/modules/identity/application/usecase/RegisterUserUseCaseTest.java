package com.questhub.modules.identity.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.questhub.modules.identity.application.command.RegisterUserCommand;
import com.questhub.modules.identity.domain.user.Email;
import com.questhub.modules.identity.domain.user.Role;
import com.questhub.modules.identity.domain.user.User;
import com.questhub.modules.identity.domain.user.UserRepository;
import com.questhub.modules.identity.domain.user.Username;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import com.questhub.shared.domain.FieldErrorItem;
import com.questhub.shared.outbox.OutboxPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private OutboxPublisher outboxPublisher;

  @InjectMocks private RegisterUserUseCase useCase;

  @Test
  void register_whenEmailFree_shouldCreateAndSaveUserWithEncodedPassword() {
    when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
    when(userRepository.existsByUsername(any(Username.class))).thenReturn(false);
    when(passwordEncoder.encode("secret123")).thenReturn("$2a$10$encoded-hash");
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    User created = useCase.register(request("alice@example.com", "alice"));

    assertThat(created).isNotNull();
    assertThat(created.getId()).isNotNull();
    assertThat(created.getEmail().value()).isEqualTo("alice@example.com");
    assertThat(created.getUsername().value()).isEqualTo("alice");
    assertThat(created.getDisplayName().value()).isEqualTo("Display Name");
    assertThat(created.getPasswordHash()).isEqualTo("$2a$10$encoded-hash");
    assertThat(created.getRole()).isEqualTo(Role.USER);

    ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(saved.capture());
    assertThat(saved.getValue().getPasswordHash()).isEqualTo("$2a$10$encoded-hash");
    verify(passwordEncoder).encode("secret123");
    verify(outboxPublisher)
        .publish(eq("User"), eq(created.getId()), eq("user.registered"), any());
  }

  @Test
  void register_whenEmailTaken_shouldThrowConflictWithEmailDetail() {
    when(userRepository.existsByEmail(any(Email.class))).thenReturn(true);

    BusinessException ex =
        catchThrowableOfType(
            () -> useCase.register(request("alice@example.com", "alice")),
            BusinessException.class);

    assertThat(ex.getCode()).isEqualTo(ErrorCodes.CONFLICT);
    assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(ex.getDetails()).extracting(FieldErrorItem::field).containsExactly("email");
    verify(userRepository, never()).save(any());
  }

  @Test
  void register_whenUsernameTaken_shouldThrowConflictWithUsernameDetail() {
    when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
    when(userRepository.existsByUsername(any(Username.class))).thenReturn(true);

    BusinessException ex =
        catchThrowableOfType(
            () -> useCase.register(request("alice@example.com", "alice")),
            BusinessException.class);

    assertThat(ex.getCode()).isEqualTo(ErrorCodes.CONFLICT);
    assertThat(ex.getDetails()).extracting(FieldErrorItem::field).containsExactly("username");
    verify(userRepository, never()).save(any());
  }

  @Test
  void register_whenBothTaken_shouldGatherBothConflictsInOneThrow() {
    when(userRepository.existsByEmail(any(Email.class))).thenReturn(true);
    when(userRepository.existsByUsername(any(Username.class))).thenReturn(true);

    BusinessException ex =
        catchThrowableOfType(
            () -> useCase.register(request("alice@example.com", "alice")),
            BusinessException.class);

    assertThat(ex.getDetails())
        .extracting(FieldErrorItem::field)
        .containsExactlyInAnyOrder("email", "username");
    assertThat(ex.getDetails())
        .extracting(FieldErrorItem::message)
        .contains("Email đã được đăng ký", "Username đã tồn tại");
    verify(userRepository, never()).save(any());
  }

  private RegisterUserCommand request(String email, String username) {
    return new RegisterUserCommand(email, username, "Display Name", "secret123");
  }
}

