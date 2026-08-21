package com.questhub.modules.quest.infrastructure.persistence.chapter;

import com.questhub.modules.quest.infrastructure.persistence.quest.QuestJpaEntity;
import com.questhub.modules.quest.infrastructure.persistence.task.TaskJpaEntity;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "chapters")
public class ChapterJpaEntity {

  @Id
  @Column(name = "id")
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "quest_id", nullable = false)
  private QuestJpaEntity quest;

  @Column(name = "title", nullable = false, length = 100)
  private String title;

  @Column(name = "description", length = 1000)
  private String description;

  @Column(name = "position", nullable = false)
  private int position;

  @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @OrderBy("order")
  private List<TaskJpaEntity> tasks = new ArrayList<>();

  protected ChapterJpaEntity() {}

  public ChapterJpaEntity(UUID id, String title, String description, int position) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.position = position;
  }

  public void setQuest(QuestJpaEntity quest) {
    this.quest = quest;
  }

  public UUID getId() {
    return id;
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

  public List<TaskJpaEntity> getTasks() {
    return tasks;
  }
}

