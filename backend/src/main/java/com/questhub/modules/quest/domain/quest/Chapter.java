package com.questhub.modules.quest.domain.quest;

import com.questhub.shared.domain.DomainValidationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Chapter {

  private final UUID id;
  private String title;
  private String description;
  private int position;
  private final List<Task> tasks = new ArrayList<>();
  private final Instant createdAt;
  private Instant updatedAt;

  private Chapter(
      UUID id,
      String title,
      String description,
      int position,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.position = position;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static Chapter create(String title, String description, int position) {
    Instant now = Instant.now();
    return new Chapter(UUID.randomUUID(), title, description, position, now, now);
  }

  public static Chapter restore(
      UUID id,
      String title,
      String description,
      int position,
      List<Task> tasks,
      Instant createdAt,
      Instant updatedAt) {
    Chapter chapter = new Chapter(id, title, description, position, createdAt, updatedAt);
    chapter.tasks.addAll(tasks);
    return chapter;
  }

  public void addTask(Task task) {
    task.changeOrder(tasks.size());
    tasks.add(task);
    this.updatedAt = Instant.now();
  }

  public void removeTask(UUID taskId) {
    tasks.removeIf(task -> task.getId().equals(taskId));
    for (int i = 0; i < tasks.size(); i++) {
      tasks.get(i).changeOrder(i);
    }
    this.updatedAt = Instant.now();
  }

  public void reorderTasks(List<UUID> taskIds) {
    if (taskIds.size() != tasks.size()) {
      throw new DomainValidationException("Reorder list must match current task count");
    }
    for (UUID id : taskIds) {
      if (tasks.stream().noneMatch(task -> task.getId().equals(id))) {
        throw new DomainValidationException("Reorder list contains unknown task: " + id);
      }
    }
    for (int i = 0; i < taskIds.size(); i++) {
      UUID id = taskIds.get(i);
      int newOrder = i;
      tasks.stream()
          .filter(task -> task.getId().equals(id))
          .findFirst()
          .ifPresent(task -> task.changeOrder(newOrder));
    }
    this.updatedAt = Instant.now();
  }

  public void changePosition(int position) {
    this.position = position;
    this.updatedAt = Instant.now();
  }

  public void updateDetails(String title, String description) {
    this.title = title;
    this.description = description;
    this.updatedAt = Instant.now();
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

  public List<Task> getTasks() {
    return Collections.unmodifiableList(tasks);
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}