package com.questhub.modules.identity.domain.user;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class User {
  private final UUID id;
  private final Instant createdAt;
  private Email email;
  private Username username;
  private DisplayName displayName;
  private String passwordHash;
  private Role role;
  private String avatarUrl;
  private String bio;
  private boolean isPublic;
  private int followerCount;
  private int followingCount;
  private Map<String, Boolean> notificationPrefs;
  private Instant updatedAt;

  private User(
      UUID id,
      Email email,
      Username username,
      DisplayName displayName,
      String passwordHash,
      Role role,
      String avatarUrl,
      String bio,
      boolean isPublic,
      int followerCount,
      int followingCount,
      Map<String, Boolean> notificationPrefs,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.email = email;
    this.username = username;
    this.displayName = displayName;
    this.passwordHash = passwordHash;
    this.role = role;
    this.avatarUrl = avatarUrl;
    this.bio = bio;
    this.isPublic = isPublic;
    this.followerCount = followerCount;
    this.followingCount = followingCount;
    this.notificationPrefs = notificationPrefs;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static User create(
      Email email, Username username, DisplayName displayName, String passwordHash) {
    Instant now = Instant.now();
    return new User(
        UUID.randomUUID(),
        email,
        username,
        displayName,
        passwordHash,
        Role.USER,
        null,
        null,
        true,
        0,
        0,
        Map.of(),
        now,
        now);
  }

  public static User restore(
      UUID id,
      Email email,
      Username username,
      DisplayName displayName,
      String passwordHash,
      Role role,
      String avatarUrl,
      String bio,
      boolean isPublic,
      int followerCount,
      int followingCount,
      Map<String, Boolean> notificationPrefs,
      Instant createdAt,
      Instant updatedAt) {
    return new User(
        id,
        email,
        username,
        displayName,
        passwordHash,
        role,
        avatarUrl,
        bio,
        isPublic,
        followerCount,
        followingCount,
        notificationPrefs,
        createdAt,
        updatedAt);
  }

  public void updateProfile(String avatarUrl, String bio, DisplayName displayName, boolean isPublic) {
    this.avatarUrl = avatarUrl;
    this.bio = bio;
    this.displayName = displayName;
    this.isPublic = isPublic;
    this.updatedAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public Email getEmail() {
    return email;
  }

  public Username getUsername() {
    return username;
  }

  public DisplayName getDisplayName() {
    return displayName;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public Role getRole() {
    return role;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public String getBio() {
    return bio;
  }

  public boolean isPublic() {
    return isPublic;
  }

  public int getFollowerCount() {
    return followerCount;
  }

  public int getFollowingCount() {
    return followingCount;
  }

  public Map<String, Boolean> getNotificationPrefs() {
    return notificationPrefs;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
