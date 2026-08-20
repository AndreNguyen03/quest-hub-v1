package com.questhub.modules.quest.application.usecase;

import com.questhub.modules.quest.domain.personalquest.PersonalChapter;
import com.questhub.modules.quest.domain.personalquest.PersonalQuest;
import com.questhub.modules.quest.domain.personalquest.PersonalQuestRepository;
import com.questhub.modules.quest.domain.personalquest.PersonalTask;
import com.questhub.modules.quest.domain.quest.TaskType;
import com.questhub.shared.annotation.UseCase;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class EditPersonalQuestUseCase {

  private final PersonalQuestRepository personalQuestRepository;

  @Transactional(
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public PersonalQuest addChapter(UUID personalQuestId, UUID userId, String title, String description) {
    PersonalQuest quest = loadEditable(personalQuestId, userId);
    PersonalChapter chapter = quest.addChapter(title, description);
    personalQuestRepository.save(quest);
    log.info(
        "Personal chapter added personalQuestId={} chapterId={} userId={}",
        personalQuestId, chapter.getId(), userId);
    return quest;
  }

  @Transactional(
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public PersonalQuest addTask(
      UUID personalQuestId,
      UUID chapterId,
      UUID userId,
      TaskType type,
      String title,
      String description,
      Map<String, Object> config,
      Integer order) {
    PersonalQuest quest = loadEditable(personalQuestId, userId);
    PersonalTask task = quest.addTask(chapterId, type, title, description, config, order);
    personalQuestRepository.save(quest);
    log.info(
        "Personal task added personalQuestId={} chapterId={} taskId={} userId={}",
        personalQuestId, chapterId, task.getId(), userId);
    return quest;
  }

  @Transactional(
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public PersonalQuest removeChapter(UUID personalQuestId, UUID chapterId, UUID userId) {
    PersonalQuest quest = loadEditable(personalQuestId, userId);
    quest.removeChapter(chapterId);
    personalQuestRepository.save(quest);
    log.info(
        "Personal chapter removed personalQuestId={} chapterId={} userId={}",
        personalQuestId, chapterId, userId);
    return quest;
  }

  @Transactional(
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public PersonalQuest removeTask(
      UUID personalQuestId, UUID chapterId, UUID taskId, UUID userId) {
    PersonalQuest quest = loadEditable(personalQuestId, userId);
    quest.removeTask(taskId);
    personalQuestRepository.save(quest);
    log.info(
        "Personal task removed personalQuestId={} chapterId={} taskId={} userId={}",
        personalQuestId, chapterId, taskId, userId);
    return quest;
  }

  @Transactional(
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public PersonalQuest reorderChapters(
      UUID personalQuestId, UUID userId, List<UUID> orderedIds) {
    PersonalQuest quest = loadEditable(personalQuestId, userId);
    quest.reorderChapters(orderedIds);
    personalQuestRepository.save(quest);
    log.info(
        "Personal chapters reordered personalQuestId={} userId={}", personalQuestId, userId);
    return quest;
  }

  @Transactional(
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public PersonalQuest reorderTasks(
      UUID personalQuestId, UUID chapterId, UUID userId, List<UUID> orderedIds) {
    PersonalQuest quest = loadEditable(personalQuestId, userId);
    quest.reorderTasks(chapterId, orderedIds);
    personalQuestRepository.save(quest);
    log.info(
        "Personal tasks reordered personalQuestId={} chapterId={} userId={}",
        personalQuestId, chapterId, userId);
    return quest;
  }

  private PersonalQuest loadEditable(UUID personalQuestId, UUID userId) {
    PersonalQuest quest =
        personalQuestRepository
            .findByIdAndUserId(personalQuestId, userId)
            .orElseThrow(
                () ->
                    BusinessException.notFound(
                        ErrorCodes.NOT_FOUND, "Không tìm thấy personal quest"));
    if (!quest.isActive()) {
      log.warn(
          "Personal quest not editable personalQuestId={} status={} userId={}",
          personalQuestId, quest.getStatus(), userId);
      throw BusinessException.conflict(
          ErrorCodes.CONFLICT, "Chỉ chỉnh sửa personal quest ACTIVE");
    }
    return quest;
  }
}