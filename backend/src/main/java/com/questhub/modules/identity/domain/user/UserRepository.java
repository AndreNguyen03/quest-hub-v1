package com.questhub.modules.identity.domain.user;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UUID id);
    List<User> findAllById(Collection<UUID> ids);
    Optional<User> findByEmail(Email email);
    Optional<User> findByUsername(Username username);
    boolean existsByEmail(Email email);
    boolean existsByUsername(Username username);
}
