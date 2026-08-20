package com.questhub.modules.identity.application.usecase;

import com.questhub.modules.identity.application.port.RefreshTokenStore;
import com.questhub.shared.annotation.UseCase;
import com.questhub.shared.infrastructure.security.JwtService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class LogoutUseCase {

  private final RefreshTokenStore refreshTokenStore;
  private final JwtService jwtService;

  public void logout(String refreshToken) {
    try {
      JwtService.TokenClaims claims = jwtService.parse(refreshToken);
      if (JwtService.TYPE_REFRESH.equals(claims.type())) {
        refreshTokenStore.delete(refreshToken);
        log.info("User logged out userId={}", claims.userId());
      }
    } catch (JwtException | IllegalArgumentException ignored) {
      // Idempotent: token rác cũng coi như đã logout.
    }
  }
}