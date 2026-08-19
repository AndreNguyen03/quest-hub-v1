package com.questhub.modules.identity.infrastructure.persistence;

import com.questhub.modules.identity.domain.user.Email;
import com.questhub.modules.identity.domain.user.User;
import com.questhub.modules.identity.domain.user.UserRepository;
import com.questhub.modules.identity.domain.user.Username;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UserRepositoryImpl implements UserRepository {

  private final SpringDataUserRepository jpa;

  public UserRepositoryImpl(SpringDataUserRepository jpa) {
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
  public Optional<User> findByEmail(Email email) {
    return jpa.findByEmail(email.value()).map(UserMapper::toDomain);
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
