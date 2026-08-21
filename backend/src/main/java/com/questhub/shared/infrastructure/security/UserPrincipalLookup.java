package com.questhub.shared.infrastructure.security;

import java.util.UUID;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Port implemented by the module owning user accounts (Identity). Keeps {@code shared} free of
 * any dependency on a specific bounded context — the JWT filter only needs to resolve a
 * {@link UserDetails} principal by id, not the concrete user aggregate.
 */
public interface UserPrincipalLookup {

  UserDetails loadById(UUID userId) throws UsernameNotFoundException;
}
