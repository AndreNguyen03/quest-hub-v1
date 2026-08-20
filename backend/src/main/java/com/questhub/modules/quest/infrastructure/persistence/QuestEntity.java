package com.questhub.modules.quest.infrastructure.persistence;

import com.questhub.modules.quest.domain.quest.CompletionRule;
import com.questhub.modules.quest.domain.quest.Difficulty;
import com.questhub.modules.quest.domain.quest.QuestVisibility;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "quests")
public class QuestEntity {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "learning_path_id")
  private UUID learningPathId;

  @Column(name = "creator_id", nullable = false)
  private UUID creatorId;

  @Column(name = "title", nullable = false, length = 120)
  private String title;

  @Column(name = "description", length = 2000)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "difficulty", nullable = false, length = 20)
  private Difficulty difficulty;

  @Column(name = "estimated_duration", nullable = false)
  private int estimatedDuration;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "completion_rule", nullable = false, columnDefinition = "jsonb")
  private CompletionRule completionRule;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "reward", nullable = false, columnDefinition = "jsonb")
  private Map<String, Object> reward;

  @Enumerated(EnumType.STRING)
  @Column(name = "visibility", nullable = false, length = 10)
  private QuestVisibility visibility;

  @Column(name = "published_at")
  private Instant publishedAt;

  @Column(name = "fork_count", nullable = false)
  private int forkCount;

  @Column(name = "avg_rating")
  private BigDecimal avgRating;

  @Column(name = "rating_count", nullable = false)
  private int ratingCount;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @OneToMany(mappedBy = "quest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @OrderBy("position")
  private List<ChapterEntity> chapters = new ArrayList<>();

  protected QuestEntity() {}

  public QuestEntity(
      UUID id,
      UUID learningPathId,
      UUID creatorId,
      String title,
      String description,
      Difficulty difficulty,
      int estimatedDuration,
      CompletionRule completionRule,
      Map<String, Object> reward,
      QuestVisibility visibility,
      Instant publishedAt,
      int forkCount,
      BigDecimal avgRating,
      int ratingCount,
      Instant createdAt,
      Instant updatedAt,
      List<ChapterEntity> chapters) {
    this.id = id;
    this.learningPathId = learningPathId;
    this.creatorId = creatorId;
    this.title = title;
    this.description = description;
    this.difficulty = difficulty;
    this.estimatedDuration = estimatedDuration;
    this.completionRule = completionRule;
    this.reward = reward == null ? Map.of() : reward;
    this.visibility = visibility;
    this.publishedAt = publishedAt;
    this.forkCount = forkCount;
    this.avgRating = avgRating;
    this.ratingCount = ratingCount;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.chapters = chapters == null ? new ArrayList<>() : chapters;
  }

  public UUID getId() {
    return id;
  }

  public UUID getLearningPathId() {
    return learningPathId;
  }

  public UUID getCreatorId() {
    return creatorId;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public Difficulty getDifficulty() {
    return difficulty;
  }

  public int getEstimatedDuration() {
    return estimatedDuration;
  }

  public CompletionRule getCompletionRule() {
    return completionRule;
  }

  public Map<String, Object> getReward() {
    return reward;
  }

  public QuestVisibility getVisibility() {
    return visibility;
  }

  public Instant getPublishedAt() {
    return publishedAt;
  }

  public int getForkCount() {
    return forkCount;
  }

  public BigDecimal getAvgRating() {
    return avgRating;
  }

  public int getRatingCount() {
    return ratingCount;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public List<ChapterEntity> getChapters() {
    return chapters;
  }
}