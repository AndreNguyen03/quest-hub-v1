package com.questhub.modules.identity.application.usecase;

import com.questhub.modules.identity.application.port.RefreshTokenStore;
import com.questhub.modules.identity.domain.user.User;
import com.questhub.modules.identity.domain.user.UserRepository;
import com.questhub.shared.annotation.UseCase;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import com.questhub.shared.infrastructure.security.JwtService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class RefreshUseCase {

  private final UserRepository userRepository;
  private final RefreshTokenStore refreshTokenStore;
  private final JwtService jwtService;

  public User refresh(String refreshToken) {
    JwtService.TokenClaims claims;
    try {
      claims = jwtService.parse(refreshToken);
    } catch (JwtException | IllegalArgumentException ex) {
      throw invalid();
    }
    if (!JwtService.TYPE_REFRESH.equals(claims.type())) {
      throw invalid();
    }
    if (!refreshTokenStore.isValid(refreshToken)) {
      throw invalid();
    }

    User user = userRepository.findById(claims.userId()).orElseThrow(RefreshUseCase::invalid);

    refreshTokenStore.delete(refreshToken);
    return user;
  }

  private static BusinessException invalid() {
    return BusinessException.unauthorized(
        ErrorCodes.INVALID_REFRESH_TOKEN, "Refresh token không hợp lệ");
  }
}
