package com.questhub.modules.identity.infrastructure.security;

import com.questhub.modules.identity.domain.user.User;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class QuestHubUserDetails implements UserDetails {

  private final UUID id;
  private final String username;
  private final String password;
  private final Collection<? extends GrantedAuthority> authorities;

  public QuestHubUserDetails(UUID id, String username, String password, String role) {
    this.id = id;
    this.username = username;
    this.password = password;
    this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
  }

  public static QuestHubUserDetails from(User user) {
    return new QuestHubUserDetails(
        user.getId(), user.getUsername().value(), user.getPasswordHash(), user.getRole().name());
  }

  public UUID getId() {
    return id;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
