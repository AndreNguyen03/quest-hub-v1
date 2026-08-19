package com.questhub.shared.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.questhub.shared.domain.AuthenticatedUser;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private final JwtProperties properties = jwtProperties();
    private final JwtService jwtService = new JwtService(properties);

    @Test
    void accessTokenRoundTrip() {
        AuthenticatedUser user = new AuthenticatedUser(UUID.randomUUID(), "alice");

        String token = jwtService.generateAccessToken(user, List.of("USER"));

        JwtService.TokenClaims claims = jwtService.parse(token);
        assertThat(claims.type()).isEqualTo(JwtService.TYPE_ACCESS);
        assertThat(claims.userId()).isEqualTo(user.id());
        assertThat(claims.username()).isEqualTo("alice");
        assertThat(claims.roles()).containsExactly("USER");
    }

    @Test
    void refreshTokenRoundTrip() {
        AuthenticatedUser user = new AuthenticatedUser(UUID.randomUUID(), "alice");

        String token = jwtService.generateRefreshToken(user);

        JwtService.TokenClaims claims = jwtService.parse(token);
        assertThat(claims.type()).isEqualTo(JwtService.TYPE_REFRESH);
        assertThat(claims.roles()).isEmpty();
    }

    private JwtProperties jwtProperties() {
        JwtProperties p = new JwtProperties();
        p.setSecret("test-secret-test-secret-test-secret-test-secret-0123456789abcdef");
        p.setIssuer("questhub");
        return p;
    }
}