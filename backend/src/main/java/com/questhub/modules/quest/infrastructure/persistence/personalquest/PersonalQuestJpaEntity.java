package com.questhub.modules.quest.infrastructure.persistence.personalquest;

import com.questhub.modules.quest.domain.personalquest.PersonalQuestStatus;
import com.questhub.modules.quest.domain.quest.CompletionRule;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "personal_quests")
public class PersonalQuestJpaEntity {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "quest_id")
  private UUID questId;

  @Column(name = "learning_path_id")
  private UUID learningPathId;

  @Column(name = "title", nullable = false, length = 100)
  private String title;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "completion_rule", nullable = false, columnDefinition = "jsonb")
  private CompletionRule completionRule;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 10)
  private PersonalQuestStatus status;

  @Column(name = "progress", nullable = false)
  private int progress;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "completed_at")
  private Instant completedAt;

  @OneToMany(mappedBy = "personalQuest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @OrderBy("position")
  private List<PersonalChapterJpaEntity> chapters = new ArrayList<>();

  protected PersonalQuestJpaEntity() {}

  public PersonalQuestJpaEntity(
      UUID id,
      UUID userId,
      UUID questId,
      UUID learningPathId,
      String title,
      CompletionRule completionRule,
      PersonalQuestStatus status,
      int progress,
      Instant createdAt,
      Instant updatedAt,
      Instant completedAt,
      List<PersonalChapterJpaEntity> chapters) {
    this.id = id;
    this.userId = userId;
    this.questId = questId;
    this.learningPathId = learningPathId;
    this.title = title;
    this.completionRule = completionRule;
    this.status = status;
    this.progress = progress;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.completedAt = completedAt;
    this.chapters = chapters == null ? new ArrayList<>() : chapters;
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public UUID getQuestId() {
    return questId;
  }

  public UUID getLearningPathId() {
    return learningPathId;
  }

  public String getTitle() {
    return title;
  }

  public CompletionRule getCompletionRule() {
    return completionRule;
  }

  public PersonalQuestStatus getStatus() {
    return status;
  }

  public int getProgress() {
    return progress;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public Instant getCompletedAt() {
    return completedAt;
  }

  public List<PersonalChapterJpaEntity> getChapters() {
    return chapters;
  }
}






