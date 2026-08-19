package com.questhub.modules.identity.application.service;

import com.questhub.modules.identity.application.port.RefreshTokenStore;
import com.questhub.modules.identity.domain.user.User;
import com.questhub.modules.identity.interfaces.dto.TokenPair;
import com.questhub.shared.domain.AuthenticatedUser;
import com.questhub.shared.infrastructure.security.JwtProperties;
import com.questhub.shared.infrastructure.security.JwtService;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TokenIssuer {

  private final JwtService jwtService;
  private final RefreshTokenStore refreshTokenStore;
  private final JwtProperties jwtProperties;

  public TokenIssuer(
      JwtService jwtService, RefreshTokenStore refreshTokenStore, JwtProperties jwtProperties) {
    this.jwtService = jwtService;
    this.refreshTokenStore = refreshTokenStore;
    this.jwtProperties = jwtProperties;
  }

  public TokenPair issue(User user) {
    AuthenticatedUser authenticated =
        new AuthenticatedUser(user.getId(), user.getUsername().value());
    String accessToken =
        jwtService.generateAccessToken(authenticated, List.of(user.getRole().name()));
    String refreshToken = jwtService.generateRefreshToken(authenticated);
    refreshTokenStore.store(user.getId(), refreshToken, jwtProperties.getRefreshTokenTtl());
    return new TokenPair(accessToken, refreshToken);
  }
}
