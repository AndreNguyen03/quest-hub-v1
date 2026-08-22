package com.questhub.modules.world.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.questhub.modules.world.domain.achievement.Achievement;
import com.questhub.modules.world.domain.achievement.AchievementRepository;
import com.questhub.modules.world.domain.achievement.UserAchievement;
import com.questhub.modules.world.domain.achievement.UserAchievementRepository;
import com.questhub.modules.world.domain.district.District;
import com.questhub.modules.world.domain.district.DistrictRepository;
import com.questhub.modules.world.domain.world.World;
import com.questhub.modules.world.domain.world.WorldRepository;
import com.questhub.shared.outbox.OutboxPublisher;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AchievementUnlockServiceTest {

  @Mock private AchievementRepository achievementRepository;
  @Mock private UserAchievementRepository userAchievementRepository;
  @Mock private WorldRepository worldRepository;
  @Mock private DistrictRepository districtRepository;
  @Mock private OutboxPublisher outboxPublisher;

  @InjectMocks private AchievementUnlockService service;

  @Test
  void evaluate_questCountCriteriaMet_shouldUnlockAndPublish() {
    UUID userId = UUID.randomUUID();
    World world = World.restore(UUID.randomUUID(), userId, "user1", Instant.now(), 5);
    Achievement ach = achievement("FIVE_QUESTS", Map.of("type", "QUEST_COUNT", "threshold", 5));

    when(worldRepository.findByUserId(userId)).thenReturn(Optional.of(world));
    when(districtRepository.findByWorldId(world.getId())).thenReturn(List.of());
    when(achievementRepository.findAll()).thenReturn(List.of(ach));
    when(userAchievementRepository.findAchievementIdsByUserId(userId)).thenReturn(Set.of());

    service.evaluate(userId);

    verify(userAchievementRepository).save(any(UserAchievement.class));
    verify(outboxPublisher).publish(eq("World"), eq(userId), eq("achievement.unlocked"), any());
  }

  @Test
  void evaluate_questCountBelowThreshold_shouldNotUnlock() {
    UUID userId = UUID.randomUUID();
    World world = World.restore(UUID.randomUUID(), userId, "user1", Instant.now(), 2);
    Achievement ach = achievement("FIVE_QUESTS", Map.of("type", "QUEST_COUNT", "threshold", 5));

    when(worldRepository.findByUserId(userId)).thenReturn(Optional.of(world));
    when(districtRepository.findByWorldId(world.getId())).thenReturn(List.of());
    when(achievementRepository.findAll()).thenReturn(List.of(ach));
    when(userAchievementRepository.findAchievementIdsByUserId(userId)).thenReturn(Set.of());

    service.evaluate(userId);

    verify(userAchievementRepository, never()).save(any());
    verify(outboxPublisher, never()).publish(any(), any(), any(), any());
  }

  @Test
  void evaluate_taskCountCriteriaMet_shouldUnlock() {
    UUID userId = UUID.randomUUID();
    UUID worldId = UUID.randomUUID();
    World world = World.restore(worldId, userId, "user1", Instant.now(), 0);
    District d1 = District.restore(UUID.randomUUID(), worldId, UUID.randomUUID(), 7, Instant.now(), Instant.now());
    District d2 = District.restore(UUID.randomUUID(), worldId, UUID.randomUUID(), 5, Instant.now(), Instant.now());
    Achievement ach = achievement("TEN_TASKS", Map.of("type", "TASK_COUNT", "threshold", 10));

    when(worldRepository.findByUserId(userId)).thenReturn(Optional.of(world));
    when(districtRepository.findByWorldId(worldId)).thenReturn(List.of(d1, d2));
    when(achievementRepository.findAll()).thenReturn(List.of(ach));
    when(userAchievementRepository.findAchievementIdsByUserId(userId)).thenReturn(Set.of());

    service.evaluate(userId);

    verify(userAchievementRepository).save(any(UserAchievement.class));
  }

  @Test
  void evaluate_domainTaskCountCriteriaMet_shouldUnlockWhenAnyDomainMeetsThreshold() {
    UUID userId = UUID.randomUUID();
    UUID worldId = UUID.randomUUID();
    World world = World.restore(worldId, userId, "user1", Instant.now(), 0);
    District prog = District.restore(UUID.randomUUID(), worldId, UUID.randomUUID(), 5, Instant.now(), Instant.now());
    District lang = District.restore(UUID.randomUUID(), worldId, UUID.randomUUID(), 2, Instant.now(), Instant.now());
    Achievement ach = achievement("DOMAIN_FIVE", Map.of("type", "DOMAIN_TASK_COUNT", "threshold", 5));

    when(worldRepository.findByUserId(userId)).thenReturn(Optional.of(world));
    when(districtRepository.findByWorldId(worldId)).thenReturn(List.of(prog, lang));
    when(achievementRepository.findAll()).thenReturn(List.of(ach));
    when(userAchievementRepository.findAchievementIdsByUserId(userId)).thenReturn(Set.of());

    service.evaluate(userId);

    verify(userAchievementRepository).save(any(UserAchievement.class));
  }

  @Test
  void evaluate_alreadyUnlocked_shouldSkip() {
    UUID userId = UUID.randomUUID();
    UUID achId = UUID.randomUUID();
    World world = World.restore(UUID.randomUUID(), userId, "user1", Instant.now(), 5);
    Achievement ach = Achievement.restore(achId, "FIVE_QUESTS", "Five Quests", null,
        Map.of("type", "QUEST_COUNT", "threshold", 5), Instant.now());

    when(worldRepository.findByUserId(userId)).thenReturn(Optional.of(world));
    when(districtRepository.findByWorldId(world.getId())).thenReturn(List.of());
    when(achievementRepository.findAll()).thenReturn(List.of(ach));
    when(userAchievementRepository.findAchievementIdsByUserId(userId)).thenReturn(Set.of(achId));

    service.evaluate(userId);

    verify(userAchievementRepository, never()).save(any());
    verify(outboxPublisher, never()).publish(any(), any(), any(), any());
  }

  @Test
  void evaluate_noWorld_shouldReturnEarly() {
    UUID userId = UUID.randomUUID();
    when(worldRepository.findByUserId(userId)).thenReturn(Optional.empty());

    service.evaluate(userId);

    verify(achievementRepository, never()).findAll();
    verify(userAchievementRepository, never()).save(any());
  }

  @Test
  void evaluate_multipleAchievementsMet_shouldUnlockAll() {
    UUID userId = UUID.randomUUID();
    UUID worldId = UUID.randomUUID();
    World world = World.restore(worldId, userId, "user1", Instant.now(), 5);
    District d = District.restore(UUID.randomUUID(), worldId, UUID.randomUUID(), 10, Instant.now(), Instant.now());
    Achievement ach1 = achievement("FIVE_QUESTS", Map.of("type", "QUEST_COUNT", "threshold", 5));
    Achievement ach2 = achievement("TEN_TASKS", Map.of("type", "TASK_COUNT", "threshold", 10));

    when(worldRepository.findByUserId(userId)).thenReturn(Optional.of(world));
    when(districtRepository.findByWorldId(worldId)).thenReturn(List.of(d));
    when(achievementRepository.findAll()).thenReturn(List.of(ach1, ach2));
    when(userAchievementRepository.findAchievementIdsByUserId(userId)).thenReturn(Set.of());

    service.evaluate(userId);

    verify(userAchievementRepository, times(2)).save(any(UserAchievement.class));
    verify(outboxPublisher, times(2)).publish(eq("World"), eq(userId), eq("achievement.unlocked"), any());
  }

  private Achievement achievement(String code, Map<String, Object> criteria) {
    return Achievement.restore(UUID.randomUUID(), code, code, null, criteria, Instant.now());
  }
}
