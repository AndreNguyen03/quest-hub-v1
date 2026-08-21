package com.questhub.modules.quest.domain.personalquest;

import com.questhub.modules.quest.domain.quest.CompletionRule;
import com.questhub.modules.quest.domain.task.TaskType;
import com.questhub.shared.domain.DomainValidationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class PersonalQuest {

  private final UUID id;
  private final UUID userId;
  private final UUID questId;
  private final UUID learningPathId;
  private final String title;
  private final CompletionRule completionRule;
  private PersonalQuestStatus status;
  private int progress;
  private final List<PersonalChapter> chapters = new ArrayList<>();
  private final Instant createdAt;
  private Instant updatedAt;
  private Instant completedAt;

  private PersonalQuest(
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
      Instant completedAt) {
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
  }

  public static PersonalQuest create(
      UUID userId,
      UUID questId,
      UUID learningPathId,
      String title,
      CompletionRule completionRule) {
    Instant now = Instant.now();
    return new PersonalQuest(
        UUID.randomUUID(), userId, questId, learningPathId, title, completionRule,
        PersonalQuestStatus.ACTIVE, 0, now, now, null);
  }

  public static PersonalQuest restore(
      UUID id,
      UUID userId,
      UUID questId,
      UUID learningPathId,
      String title,
      CompletionRule completionRule,
      PersonalQuestStatus status,
      int progress,
      List<PersonalChapter> chapters,
      Instant createdAt,
      Instant updatedAt,
      Instant completedAt) {
    PersonalQuest quest =
        new PersonalQuest(
            id, userId, questId, learningPathId, title, completionRule, status, progress, createdAt, updatedAt, completedAt);
    quest.chapters.addAll(chapters);
    return quest;
  }

  public void addChapter(PersonalChapter chapter) {
    chapters.add(chapter);
    this.updatedAt = Instant.now();
  }

  public Optional<PersonalTask> findTask(UUID personalTaskId) {
    for (PersonalChapter chapter : chapters) {
      Optional<PersonalTask> task =
          chapter.getTasks().stream().filter(t -> t.getId().equals(personalTaskId)).findFirst();
      if (task.isPresent()) {
        return task;
      }
    }
    return Optional.empty();
  }

  public Optional<PersonalChapter> findChapterOf(UUID personalTaskId) {
    return chapters.stream()
        .filter(c -> c.getTasks().stream().anyMatch(t -> t.getId().equals(personalTaskId)))
        .findFirst();
  }

  public void completeTask(UUID personalTaskId, Map<String, Object> evidence) {
    PersonalTask task =
        findTask(personalTaskId)
            .orElseThrow(
                () -> new DomainValidationException("Không tìm thấy task trong personal quest"));
    task.complete(evidence);
    recalculateProgress();
    this.updatedAt = Instant.now();
  }

  public boolean undoTask(UUID personalTaskId) {
    PersonalTask task =
        findTask(personalTaskId)
            .orElseThrow(
                () -> new DomainValidationException("Không tìm thấy task trong personal quest"));
    if (!task.isCompleted()) {
      return false;
    }
    task.undo();
    recalculateProgress();
    this.updatedAt = Instant.now();
    return true;
  }

  public void completeTaskByQuiz(UUID personalTaskId) {
    PersonalTask task =
        findTask(personalTaskId)
            .orElseThrow(
                () -> new DomainValidationException("Không tìm thấy task trong personal quest"));
    task.completeQuiz();
    recalculateProgress();
    this.updatedAt = Instant.now();
  }

  public PersonalChapter addChapter(String title, String description) {
    int nextPosition =
        chapters.stream().mapToInt(PersonalChapter::getPosition).max().orElse(-1) + 1;
    PersonalChapter chapter = PersonalChapter.create(null, title, description, nextPosition);
    chapters.add(chapter);
    this.updatedAt = Instant.now();
    return chapter;
  }

  public PersonalTask addTask(
      UUID chapterId,
      TaskType type,
      String title,
      String description,
      Map<String, Object> config,
      Integer order) {
    PersonalChapter chapter =
        chapters.stream()
            .filter(c -> c.getId().equals(chapterId))
            .findFirst()
            .orElseThrow(() -> new DomainValidationException("Không tìm thấy chapter"));
    int nextOrder =
        order != null
            ? order
            : chapter.getTasks().stream().mapToInt(PersonalTask::getOrder).max().orElse(-1) + 1;
    PersonalTask task = PersonalTask.create(null, type, title, description, nextOrder, config);
    chapter.addTask(task);
    recalculateProgress();
    this.updatedAt = Instant.now();
    return task;
  }

  public void removeChapter(UUID chapterId) {
    boolean removed = chapters.removeIf(c -> c.getId().equals(chapterId));
    if (!removed) {
      throw new DomainValidationException("Không tìm thấy chapter");
    }
    recalculateProgress();
    this.updatedAt = Instant.now();
  }

  public void removeTask(UUID taskId) {
    for (PersonalChapter chapter : chapters) {
      if (chapter.removeTask(taskId)) {
        recalculateProgress();
        this.updatedAt = Instant.now();
        return;
      }
    }
    throw new DomainValidationException("Không tìm thấy task");
  }

  public void reorderChapters(List<UUID> orderedIds) {
    Set<UUID> currentIds =
        chapters.stream().map(PersonalChapter::getId).collect(Collectors.toSet());
    if (orderedIds.size() != chapters.size() || !currentIds.equals(new HashSet<>(orderedIds))) {
      throw new DomainValidationException("Danh sách chapter không khớp với personal quest");
    }
    for (int i = 0; i < orderedIds.size(); i++) {
      UUID id = orderedIds.get(i);
      chapters.stream()
          .filter(c -> c.getId().equals(id))
          .findFirst()
          .orElseThrow()
          .reorderTo(i);
    }
    chapters.sort(Comparator.comparingInt(PersonalChapter::getPosition));
    this.updatedAt = Instant.now();
  }

  public void reorderTasks(UUID chapterId, List<UUID> orderedIds) {
    PersonalChapter chapter =
        chapters.stream()
            .filter(c -> c.getId().equals(chapterId))
            .findFirst()
            .orElseThrow(() -> new DomainValidationException("Không tìm thấy chapter"));
    Set<UUID> currentIds =
        chapter.getTasks().stream().map(PersonalTask::getId).collect(Collectors.toSet());
    if (orderedIds.size() != chapter.getTasks().size() || !currentIds.equals(new HashSet<>(orderedIds))) {
      throw new DomainValidationException("Danh sách task không khớp với chapter");
    }
    for (int i = 0; i < orderedIds.size(); i++) {
      UUID id = orderedIds.get(i);
      chapter.getTasks().stream()
          .filter(t -> t.getId().equals(id))
          .findFirst()
          .orElseThrow()
          .reorderTo(i);
    }
    chapter.sortTasks();
    this.updatedAt = Instant.now();
  }

  private void recalculateProgress() {
    long total = chapters.stream().flatMap(c -> c.getTasks().stream()).count();
    if (total == 0) {
      this.progress = 0;
      return;
    }
    long done =
        chapters.stream().flatMap(c -> c.getTasks().stream()).filter(PersonalTask::isCompleted).count();
    this.progress = (int) Math.round(done * 100.0 / total);
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

  public List<PersonalChapter> getChapters() {
    return Collections.unmodifiableList(chapters);
  }

  public List<PersonalTask> getAllTasks() {
    return chapters.stream().flatMap(c -> c.getTasks().stream()).toList();
  }

  public boolean isCompleted() {
    return status == PersonalQuestStatus.COMPLETED;
  }

  public boolean isActive() {
    return status == PersonalQuestStatus.ACTIVE;
  }

  public void markCompleted() {
    if (isCompleted()) {
      return;
    }
    this.status = PersonalQuestStatus.COMPLETED;
    this.completedAt = Instant.now();
    this.updatedAt = completedAt;
  }

  public void reopen() {
    if (!isCompleted()) {
      return;
    }
    this.status = PersonalQuestStatus.ACTIVE;
    this.completedAt = null;
    this.updatedAt = Instant.now();
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
}


