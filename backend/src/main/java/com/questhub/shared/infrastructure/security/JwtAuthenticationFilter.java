package com.questhub.shared.infrastructure.security;

import com.questhub.shared.domain.AuthenticatedUser;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtService jwtService;
  private final UserPrincipalLookup userPrincipalLookup;

  public JwtAuthenticationFilter(JwtService jwtService, UserPrincipalLookup userPrincipalLookup) {
    this.jwtService = jwtService;
    this.userPrincipalLookup = userPrincipalLookup;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith(BEARER_PREFIX)) {
      try {
        JwtService.TokenClaims claims = jwtService.parse(header.substring(BEARER_PREFIX.length()));
        if (JwtService.TYPE_ACCESS.equals(claims.type())) {
          UserDetails userDetails = userPrincipalLookup.loadById(claims.userId());
          AuthenticatedUser principal =
              new AuthenticatedUser(claims.userId(), userDetails.getUsername());
          SecurityContextHolder.getContext()
              .setAuthentication(
                  new UsernamePasswordAuthenticationToken(
                      principal, null, userDetails.getAuthorities()));
        }
      } catch (JwtException | IllegalArgumentException | UsernameNotFoundException ex) {
        SecurityContextHolder.clearContext();
      }
    }
    filterChain.doFilter(request, response);
  }
}
