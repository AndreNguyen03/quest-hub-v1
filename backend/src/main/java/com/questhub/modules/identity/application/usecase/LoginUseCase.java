package com.questhub.modules.identity.application.usecase;

import com.questhub.modules.identity.application.request.LoginRequest;
import com.questhub.modules.identity.domain.user.Email;
import com.questhub.modules.identity.domain.user.User;
import com.questhub.modules.identity.domain.user.UserRepository;
import com.questhub.shared.annotation.UseCase;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@UseCase
@RequiredArgsConstructor
public class LoginUseCase {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public User login(LoginRequest request) {
    User user =
        userRepository
            .findByEmail(new Email(request.email()))
            .orElseThrow(
                () ->
                    BusinessException.unauthorized(
                        ErrorCodes.INVALID_CREDENTIALS, "Email hoặc mật khẩu không đúng"));

    if (user.getPasswordHash() == null
        || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
      throw BusinessException.unauthorized(
          ErrorCodes.INVALID_CREDENTIALS, "Email hoặc mật khẩu không đúng");
    }

    return user;
  }
}