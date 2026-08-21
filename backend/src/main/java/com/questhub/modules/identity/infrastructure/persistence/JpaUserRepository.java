package com.questhub.modules.identity.infrastructure.persistence;

import com.questhub.modules.identity.domain.user.Email;
import com.questhub.modules.identity.domain.user.User;
import com.questhub.modules.identity.domain.user.UserRepository;
import com.questhub.modules.identity.domain.user.Username;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JpaUserRepository implements UserRepository {

  private final SpringDataUserRepository jpa;

  public JpaUserRepository(SpringDataUserRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public User save(User user) {
    return UserMapper.toDomain(jpa.save(UserMapper.toEntity(user)));
  }

  @Override
  public Optional<User> findById(UUID id) {
    return jpa.findById(id).map(UserMapper::toDomain);
  }

  @Override
  public List<User> findAllById(Collection<UUID> ids) {
    return jpa.findAllById(ids).stream().map(UserMapper::toDomain).toList();
  }

  @Override
  public Optional<User> findByEmail(Email email) {
    return jpa.findByEmail(email.value()).map(UserMapper::toDomain);
  }

  @Override
  public Optional<User> findByUsername(Username username) {
    return jpa.findByUsername(username.value()).map(UserMapper::toDomain);
  }

  @Override
  public boolean existsByEmail(Email email) {
    return jpa.existsByEmail(email.value());
  }

  @Override
  public boolean existsByUsername(Username username) {
    return jpa.existsByUsername(username.value());
  }
}

