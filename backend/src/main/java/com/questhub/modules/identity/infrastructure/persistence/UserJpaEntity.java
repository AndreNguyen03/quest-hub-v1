package com.questhub.modules.identity.infrastructure.persistence;

import com.questhub.modules.identity.domain.user.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "users")
public class UserJpaEntity {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "email", nullable = false, length = 100)
  private String email;

  @Column(name = "username", nullable = false, length = 100)
  private String username;

  @Column(name = "display_name", nullable = false, length = 100)
  private String displayName;

  @Column(name = "password_hash")
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false, length = 10)
  private Role role;

  @Column(name = "avatar_url", length = 255)
  private String avatarUrl;

  @Column(name = "bio", length = 300)
  private String bio;

  @Column(name = "is_public", nullable = false)
  private boolean isPublic;

  @Column(name = "follower_count", nullable = false)
  private int followerCount;

  @Column(name = "following_count", nullable = false)
  private int followingCount;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "notification_prefs", nullable = false, columnDefinition = "jsonb")
  private Map<String, Boolean> notificationPrefs;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected UserJpaEntity() {}

  public UserJpaEntity(
      UUID id,
      String email,
      String username,
      String displayName,
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

  public UUID getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getUsername() {
    return username;
  }

  public String getDisplayName() {
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
