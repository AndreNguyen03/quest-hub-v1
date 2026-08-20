package com.questhub.modules.quest.infrastructure.persistence;

import com.questhub.modules.quest.domain.quest.TaskType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "tasks")
public class TaskEntity {

  @Id
  @Column(name = "id")
  private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 20)
  private TaskType type;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "chapter_id", nullable = false)
  private ChapterEntity chapter;

  @Column(name = "title", nullable = false, length = 100)
  private String title;

  @Column(name = "description", length = 1000)
  private String description;

  @Column(name = "\"order\"", nullable = false)
  private int order;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "config", nullable = false, columnDefinition = "jsonb")
  private Map<String, Object> config;

  @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @OrderBy("id")
  private List<ResourceEntity> resources = new ArrayList<>();

  protected TaskEntity() {}

  public TaskEntity(
      UUID id,
      TaskType type,
      String title,
      String description,
      int order,
      Map<String, Object> config) {
    this.id = id;
    this.type = type;
    this.title = title;
    this.description = description;
    this.order = order;
    this.config = config == null ? Map.of() : config;
  }

  public void setChapter(ChapterEntity chapter) {
    this.chapter = chapter;
  }

  public UUID getId() {
    return id;
  }

  public TaskType getType() {
    return type;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public int getOrder() {
    return order;
  }

  public Map<String, Object> getConfig() {
    return config;
  }

  public List<ResourceEntity> getResources() {
    return resources;
  }
}