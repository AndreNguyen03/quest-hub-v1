package com.questhub.shared.infrastructure.security;

import com.questhub.shared.domain.AuthenticatedUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  public static final String TYPE_ACCESS = "access";
  public static final String TYPE_REFRESH = "refresh";

  private final JwtProperties properties;
  private final SecretKey key;

  public JwtService(JwtProperties properties) {
    this.properties = properties;
    this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
  }

  public String generateAccessToken(AuthenticatedUser user, List<String> roles) {
    return buildToken(user, roles, TYPE_ACCESS, properties.getAccessTokenTtl());
  }

  public String generateRefreshToken(AuthenticatedUser user) {
    return buildToken(user, List.of(), TYPE_REFRESH, properties.getRefreshTokenTtl());
  }

  public TokenClaims parse(String token) {
    Claims claims =
        Jwts.parser()
            .verifyWith(key)
            .requireIssuer(properties.getIssuer())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    return new TokenClaims(
        UUID.fromString(claims.getSubject()),
        claims.get("username", String.class),
        toList(claims.get("roles")),
        claims.get("type", String.class),
        claims.getExpiration().toInstant());
  }

  private String buildToken(AuthenticatedUser user, List<String> roles, String type, Duration ttl) {
    Instant now = Instant.now();
    return Jwts.builder()
        .id(UUID.randomUUID().toString())
        .subject(user.id().toString())
        .claim("username", user.username())
        .claim("roles", roles)
        .claim("type", type)
        .issuer(properties.getIssuer())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(ttl)))
        .signWith(key)
        .compact();
  }

  private List<String> toList(Object value) {
    if (value == null) {
      return List.of();
    }
    if (value instanceof List<?> raw) {
      return raw.stream().map(Object::toString).toList();
    }
    return List.of();
  }

  public record TokenClaims(
      UUID userId, String username, List<String> roles, String type, Instant expiresAt) {}
}
