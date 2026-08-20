package com.questhub.modules.identity.application.usecase;

import com.questhub.modules.identity.application.request.RegisterUserRequest;
import com.questhub.modules.identity.domain.user.DisplayName;
import com.questhub.modules.identity.domain.user.Email;
import com.questhub.modules.identity.domain.user.User;
import com.questhub.modules.identity.domain.user.UserRepository;
import com.questhub.modules.identity.domain.user.Username;
import com.questhub.shared.annotation.UseCase;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import com.questhub.shared.domain.FieldErrorItem;
import com.questhub.shared.outbox.OutboxPublisher;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class RegisterUserUseCase {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final OutboxPublisher outboxPublisher;

  @Transactional(
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public User register(RegisterUserRequest request) {
    Email email = new Email(request.email());
    Username username = new Username(request.username());
    DisplayName displayName = new DisplayName(request.displayName());

    List<FieldErrorItem> conflicts = new ArrayList<>();
    if (userRepository.existsByEmail(email)) {
      conflicts.add(new FieldErrorItem("email", "Email đã được đăng ký"));
    }
    if (userRepository.existsByUsername(username)) {
      conflicts.add(new FieldErrorItem("username", "Username đã tồn tại"));
    }
    if (!conflicts.isEmpty()) {
      throw BusinessException.conflict(ErrorCodes.CONFLICT, "Đăng ký không thành công", conflicts);
    }

    String passwordHash = passwordEncoder.encode(request.password());
    User newUser = User.create(email, username, displayName, passwordHash);

    User saved = userRepository.save(newUser);
    log.info("User registered userId={} username={} email={}", saved.getId(), username.value(), email.value());

    outboxPublisher.publish(
        "User",
        saved.getId(),
        "user.registered",
        Map.of(
            "userId", saved.getId().toString(),
            "username", username.value(),
            "email", email.value()));
    return saved;
  }
}
