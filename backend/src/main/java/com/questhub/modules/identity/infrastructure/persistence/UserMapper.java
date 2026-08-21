package com.questhub.modules.identity.infrastructure.persistence;

import com.questhub.modules.identity.domain.user.DisplayName;
import com.questhub.modules.identity.domain.user.Email;
import com.questhub.modules.identity.domain.user.User;
import com.questhub.modules.identity.domain.user.Username;

public final class UserMapper {

  private UserMapper() {}

  public static UserJpaEntity toEntity(User user) {
    return new UserJpaEntity(
        user.getId(),
        user.getEmail().value(),
        user.getUsername().value(),
        user.getDisplayName().value(),
        user.getPasswordHash(),
        user.getRole(),
        user.getAvatarUrl(),
        user.getBio(),
        user.isPublic(),
        user.getFollowerCount(),
        user.getFollowingCount(),
        user.getNotificationPrefs(),
        user.getCreatedAt(),
        user.getUpdatedAt());
  }

  public static User toDomain(UserJpaEntity entity) {
    return User.restore(
        entity.getId(),
        new Email(entity.getEmail()),
        new Username(entity.getUsername()),
        new DisplayName(entity.getDisplayName()),
        entity.getPasswordHash(),
        entity.getRole(),
        entity.getAvatarUrl(),
        entity.getBio(),
        entity.isPublic(),
        entity.getFollowerCount(),
        entity.getFollowingCount(),
        entity.getNotificationPrefs(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
