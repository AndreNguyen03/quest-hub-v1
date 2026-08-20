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
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
      log.warn("Refresh failed reason={}", "invalid token");
      throw invalid();
    }
    if (!JwtService.TYPE_REFRESH.equals(claims.type())) {
      log.warn("Refresh failed reason={}", "wrong token type");
      throw invalid();
    }
    if (!refreshTokenStore.isValid(refreshToken)) {
      log.warn("Refresh failed reason={} userId={}", "token revoked", claims.userId());
      throw invalid();
    }

    User user = userRepository.findById(claims.userId()).orElseThrow(RefreshUseCase::invalid);

    refreshTokenStore.delete(refreshToken);
    log.info("Refresh token rotated userId={}", user.getId());
    return user;
  }

  private static BusinessException invalid() {
    return BusinessException.unauthorized(
        ErrorCodes.INVALID_REFRESH_TOKEN, "Refresh token không hợp lệ");
  }
}
