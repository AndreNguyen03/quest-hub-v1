package com.questhub.modules.quest.domain.personalquest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class PersonalChapter {

  private final UUID id;
  private final UUID sourceChapterId;
  private String title;
  private String description;
  private int position;
  private final List<PersonalTask> tasks = new ArrayList<>();
  private final Instant createdAt;
  private Instant updatedAt;

  private PersonalChapter(
      UUID id,
      UUID sourceChapterId,
      String title,
      String description,
      int position,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.sourceChapterId = sourceChapterId;
    this.title = title;
    this.description = description;
    this.position = position;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static PersonalChapter create(
      UUID sourceChapterId, String title, String description, int position) {
    Instant now = Instant.now();
    return new PersonalChapter(UUID.randomUUID(), sourceChapterId, title, description, position, now, now);
  }

  public static PersonalChapter restore(
      UUID id,
      UUID sourceChapterId,
      String title,
      String description,
      int position,
      List<PersonalTask> tasks,
      Instant createdAt,
      Instant updatedAt) {
    PersonalChapter chapter =
        new PersonalChapter(id, sourceChapterId, title, description, position, createdAt, updatedAt);
    chapter.tasks.addAll(tasks);
    return chapter;
  }

  public void addTask(PersonalTask task) {
    tasks.add(task);
    this.updatedAt = Instant.now();
  }

  public boolean removeTask(UUID taskId) {
    boolean removed = tasks.removeIf(t -> t.getId().equals(taskId));
    if (removed) {
      this.updatedAt = Instant.now();
    }
    return removed;
  }

  public void sortTasks() {
    tasks.sort(Comparator.comparingInt(PersonalTask::getOrder));
    this.updatedAt = Instant.now();
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

  public void reorderTo(int position) {
    this.position = position;
    this.updatedAt = Instant.now();
  }

  public List<PersonalTask> getTasks() {
    return Collections.unmodifiableList(tasks);
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}