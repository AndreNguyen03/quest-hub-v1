package com.questhub.modules.identity.infrastructure.security;

import com.questhub.modules.identity.domain.user.UserRepository;
import com.questhub.modules.identity.domain.user.Username;
import com.questhub.shared.infrastructure.security.UserPrincipalLookup;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuestHubUserDetailsService implements UserDetailsService, UserPrincipalLookup {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Username u = new Username(username);
    return userRepository
        .findByUsername(u)
        .map(QuestHubUserDetails::from)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
  }

  @Override
  public UserDetails loadById(UUID id) throws UsernameNotFoundException {
    return userRepository
        .findById(id)
        .map(QuestHubUserDetails::from)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + id));
  }
}
