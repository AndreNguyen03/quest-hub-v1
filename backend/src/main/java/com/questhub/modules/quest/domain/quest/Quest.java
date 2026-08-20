package com.questhub.modules.quest.domain.quest;

import com.questhub.shared.domain.DomainValidationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Quest {

  private final UUID id;
  private final UUID creatorId;
  private UUID learningPathId;
  private String title;
  private String description;
  private Difficulty difficulty;
  private int estimatedDuration;
  private CompletionRule completionRule;
  private Map<String, Object> reward;
  private QuestVisibility visibility;
  private Instant publishedAt;
  private final List<Chapter> chapters = new ArrayList<>();
  private final Instant createdAt;
  private Instant updatedAt;

  private Quest(
      UUID id,
      UUID creatorId,
      UUID learningPathId,
      String title,
      String description,
      Difficulty difficulty,
      int estimatedDuration,
      CompletionRule completionRule,
      Map<String, Object> reward,
      QuestVisibility visibility,
      Instant publishedAt,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.creatorId = creatorId;
    this.learningPathId = learningPathId;
    this.title = title;
    this.description = description;
    this.difficulty = difficulty;
    this.estimatedDuration = estimatedDuration;
    this.completionRule = completionRule;
    this.reward = reward == null ? Map.of() : reward;
    this.visibility = visibility;
    this.publishedAt = publishedAt;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static Quest create(
      UUID creatorId,
      UUID learningPathId,
      String title,
      String description,
      Difficulty difficulty,
      Map<String, Object> reward) {
    Instant now = Instant.now();
    return new Quest(
        UUID.randomUUID(),
        creatorId,
        learningPathId,
        title,
        description,
        difficulty,
        0,
        CompletionRule.defaultAllTasks(),
        reward,
        QuestVisibility.DRAFT,
        null,
        now,
        now);
  }

  public static Quest restore(
      UUID id,
      UUID creatorId,
      UUID learningPathId,
      String title,
      String description,
      Difficulty difficulty,
      int estimatedDuration,
      CompletionRule completionRule,
      Map<String, Object> reward,
      QuestVisibility visibility,
      Instant publishedAt,
      List<Chapter> chapters,
      Instant createdAt,
      Instant updatedAt) {
    Quest quest =
        new Quest(
            id,
            creatorId,
            learningPathId,
            title,
            description,
            difficulty,
            estimatedDuration,
            completionRule,
            reward,
            visibility,
            publishedAt,
            createdAt,
            updatedAt);
    quest.chapters.addAll(chapters);
    return quest;
  }

  public void addChapter(Chapter chapter) {
    ensureDraft("add chapter");
    chapter.changePosition(chapters.size());
    chapters.add(chapter);
    recalcEstimatedDuration();
    this.updatedAt = Instant.now();
  }

  public void addTask(UUID chapterId, Task task) {
    ensureDraft("add task");
    Chapter chapter = findChapter(chapterId);
    chapter.addTask(task);
    recalcEstimatedDuration();
    this.updatedAt = Instant.now();
  }

  public void removeChapter(UUID chapterId) {
    ensureDraft("remove chapter");
    chapters.removeIf(chapter -> chapter.getId().equals(chapterId));
    renumberChapters();
    recalcEstimatedDuration();
    this.updatedAt = Instant.now();
  }

  public void removeTask(UUID chapterId, UUID taskId) {
    ensureDraft("remove task");
    Chapter chapter = findChapter(chapterId);
    chapter.removeTask(taskId);
    recalcEstimatedDuration();
    this.updatedAt = Instant.now();
  }

  public void reorderChapters(List<UUID> chapterIds) {
    ensureDraft("reorder chapters");
    if (chapterIds.size() != chapters.size()) {
      throw new DomainValidationException("Reorder list must match current chapter count");
    }
    for (int i = 0; i < chapterIds.size(); i++) {
      UUID id = chapterIds.get(i);
      Chapter chapter =
          chapters.stream()
              .filter(c -> c.getId().equals(id))
              .findFirst()
              .orElseThrow(
                  () -> new DomainValidationException("Reorder list contains unknown chapter: " + id));
      chapter.changePosition(i);
    }
    this.updatedAt = Instant.now();
  }

  public void publish() {
    if (chapters.isEmpty()) {
      throw new DomainValidationException("Quest must have at least one chapter to publish");
    }
    for (Chapter chapter : chapters) {
      if (chapter.getTasks().isEmpty()) {
        throw new DomainValidationException("Every chapter must have at least one task to publish");
      }
    }
    if (visibility != QuestVisibility.PUBLIC) {
      this.visibility = QuestVisibility.PUBLIC;
      this.publishedAt = Instant.now();
      this.updatedAt = Instant.now();
    }
  }

  public void unpublish() {
    if (visibility == QuestVisibility.PUBLIC) {
      this.visibility = QuestVisibility.DRAFT;
      this.publishedAt = null;
      this.updatedAt = Instant.now();
    }
  }

  public void setCompletionRule(CompletionRule rule) {
    ensureDraft("set completion rule");
    this.completionRule = rule;
    this.updatedAt = Instant.now();
  }

  public void updateMetadata(
      String title,
      String description,
      Difficulty difficulty,
      CompletionRule completionRule,
      Map<String, Object> reward) {
    this.title = title;
    this.description = description;
    this.difficulty = difficulty;
    if (completionRule != null) {
      this.completionRule = completionRule;
    }
    this.reward = reward == null ? Map.of() : reward;
    this.updatedAt = Instant.now();
  }

  public boolean isDraft() {
    return visibility == QuestVisibility.DRAFT;
  }

  private void ensureDraft(String operation) {
    if (visibility != QuestVisibility.DRAFT) {
      throw new DomainValidationException(
          "Cannot " + operation + " on a quest that is not in DRAFT state");
    }
  }

  private Chapter findChapter(UUID chapterId) {
    return chapters.stream()
        .filter(c -> c.getId().equals(chapterId))
        .findFirst()
        .orElseThrow(() -> new DomainValidationException("Chapter not found: " + chapterId));
  }

  private void renumberChapters() {
    for (int i = 0; i < chapters.size(); i++) {
      chapters.get(i).changePosition(i);
    }
  }

  private void recalcEstimatedDuration() {
    this.estimatedDuration =
        chapters.stream()
            .flatMap(c -> c.getTasks().stream())
            .flatMap(t -> t.getResources().stream())
            .mapToInt(r -> r.getEstimatedMinutes() == null ? 0 : r.getEstimatedMinutes())
            .sum();
  }

  public UUID getId() {
    return id;
  }

  public UUID getCreatorId() {
    return creatorId;
  }

  public UUID getLearningPathId() {
    return learningPathId;
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

  public List<Chapter> getChapters() {
    return Collections.unmodifiableList(chapters);
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}