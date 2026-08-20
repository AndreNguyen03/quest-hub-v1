package com.questhub.modules.quest.infrastructure.persistence.personalquest;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "personal_chapters")
public class PersonalChapterEntity {

  @Id
  @Column(name = "id")
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "personal_quest_id", nullable = false)
  private PersonalQuestEntity personalQuest;

  @Column(name = "source_chapter_id")
  private UUID sourceChapterId;

  @Column(name = "title", nullable = false, length = 100)
  private String title;

  @Column(name = "description", length = 1000)
  private String description;

  @Column(name = "position", nullable = false)
  private int position;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @OneToMany(mappedBy = "personalChapter", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @OrderBy("order")
  private List<PersonalTaskEntity> tasks = new ArrayList<>();

  protected PersonalChapterEntity() {}

  public PersonalChapterEntity(
      UUID id,
      UUID sourceChapterId,
      String title,
      String description,
      int position,
      Instant createdAt,
      Instant updatedAt,
      List<PersonalTaskEntity> tasks) {
    this.id = id;
    this.sourceChapterId = sourceChapterId;
    this.title = title;
    this.description = description;
    this.position = position;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.tasks = tasks == null ? new ArrayList<>() : tasks;
  }

  public void setPersonalQuest(PersonalQuestEntity personalQuest) {
    this.personalQuest = personalQuest;
  }

  public UUID getId() {
    return id;
  }

  public UUID getSourceChapterId() {
    return sourceChapterId;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public int getPosition() {
    return position;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public List<PersonalTaskEntity> getTasks() {
    return tasks;
  }
}