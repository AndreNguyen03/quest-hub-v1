package com.questhub.modules.identity.application.dto;

import com.questhub.modules.identity.domain.user.User;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String username,
    String displayName,
    String email,
    String avatarUrl,
    String bio,
    boolean isPublic,
    int followerCount,
    int followingCount,
    Instant createdAt,
    Instant updatedAt) {

  public static UserResponse from(User user) {
    return new UserResponse(
        user.getId(),
        user.getUsername().value(),
        user.getDisplayName().value(),
        user.getEmail().value(),
        user.getAvatarUrl(),
        user.getBio(),
        user.isPublic(),
        user.getFollowerCount(),
        user.getFollowingCount(),
        user.getCreatedAt(),
        user.getUpdatedAt());
  }
}

