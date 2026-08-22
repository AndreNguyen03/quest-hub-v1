package com.questhub.modules.identity.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.questhub.modules.identity.domain.user.DisplayName;
import com.questhub.modules.identity.domain.user.Email;
import com.questhub.modules.identity.domain.user.Role;
import com.questhub.modules.identity.domain.user.User;
import com.questhub.modules.identity.domain.user.UserRepository;
import com.questhub.modules.identity.domain.user.Username;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PromoteToCreatorUseCaseTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private PromoteToCreatorUseCase useCase;

  @Test
  void promote_userRole_shouldSetCreatorRoleAndSave() {
    UUID userId = UUID.randomUUID();
    User user = User.create(
        new Email("user@example.com"),
        new Username("user_test"),
        new DisplayName("User Test"),
        "hashed");
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    useCase.promote(userId);

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    assertThat(captor.getValue().getRole()).isEqualTo(Role.CREATOR);
  }

  @Test
  void promote_alreadyCreator_shouldNotSaveAgain() {
    UUID userId = UUID.randomUUID();
    User user = User.create(
        new Email("creator@example.com"),
        new Username("creator_test"),
        new DisplayName("Creator Test"),
        "hashed");
    user.promoteToCreator();
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    useCase.promote(userId);

    verify(userRepository, never()).save(any());
  }

  @Test
  void promote_userNotFound_shouldSkipGracefully() {
    UUID userId = UUID.randomUUID();
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    useCase.promote(userId);

    verify(userRepository, never()).save(any());
  }
}
