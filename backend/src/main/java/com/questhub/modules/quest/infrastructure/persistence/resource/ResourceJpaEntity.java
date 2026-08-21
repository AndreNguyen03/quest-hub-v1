package com.questhub.modules.quest.infrastructure.persistence.resource;

import com.questhub.modules.quest.domain.resource.ResourceType;
import com.questhub.modules.quest.infrastructure.persistence.task.TaskJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "resources")
public class ResourceJpaEntity {

  @Id
  @Column(name = "id")
  private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 20)
  private ResourceType type;

  @Column(name = "title", nullable = false, length = 200)
  private String title;

  @Column(name = "url", nullable = false, length = 200)
  private String url;

  @Column(name = "estimated_minutes")
  private Integer estimatedMinutes;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "task_id", nullable = false)
  private TaskJpaEntity task;

  protected ResourceJpaEntity() {}

  public ResourceJpaEntity(UUID id, ResourceType type, String title, String url, Integer estimatedMinutes) {
    this.id = id;
    this.type = type;
    this.title = title;
    this.url = url;
    this.estimatedMinutes = estimatedMinutes;
  }

  public void setTask(TaskJpaEntity task) {
    this.task = task;
  }

  public UUID getId() {
    return id;
  }

  public ResourceType getType() {
    return type;
  }

  public String getTitle() {
    return title;
  }

  public String getUrl() {
    return url;
  }

  public Integer getEstimatedMinutes() {
    return estimatedMinutes;
  }
}




