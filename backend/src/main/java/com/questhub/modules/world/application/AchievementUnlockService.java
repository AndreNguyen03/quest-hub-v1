package com.questhub.modules.world.application;

import com.questhub.modules.world.domain.achievement.Achievement;
import com.questhub.modules.world.domain.achievement.AchievementRepository;
import com.questhub.modules.world.domain.achievement.UserAchievement;
import com.questhub.modules.world.domain.achievement.UserAchievementRepository;
import com.questhub.modules.world.domain.district.District;
import com.questhub.modules.world.domain.district.DistrictRepository;
import com.questhub.modules.world.domain.world.World;
import com.questhub.modules.world.domain.world.WorldRepository;
import com.questhub.shared.outbox.OutboxPublisher;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementUnlockService {

  private final AchievementRepository achievementRepository;
  private final UserAchievementRepository userAchievementRepository;
  private final WorldRepository worldRepository;
  private final DistrictRepository districtRepository;
  private final OutboxPublisher outboxPublisher;

  @Transactional(
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public void evaluate(UUID userId) {
    Optional<World> worldOpt = worldRepository.findByUserId(userId);
    if (worldOpt.isEmpty()) {
      return;
    }
    World world = worldOpt.get();
    List<District> districts = districtRepository.findByWorldId(world.getId());

    List<Achievement> all = achievementRepository.findAll();
    Set<UUID> unlocked = userAchievementRepository.findAchievementIdsByUserId(userId);

    int questCompletedCount = world.getQuestCompletedCount();
    int taskCompletedCount = districts.stream().mapToInt(District::getCompletionCount).sum();
    Map<UUID, Integer> domainTaskCounts = districts.stream()
        .collect(Collectors.toMap(District::getDomainId, District::getCompletionCount));

    for (Achievement ach : all) {
      if (unlocked.contains(ach.getId())) {
        continue;
      }
      Map<String, Object> criteria = ach.getCriteria();
      String type = (String) criteria.get("type");
      Number thresholdNum = (Number) criteria.get("threshold");
      int threshold = thresholdNum != null ? thresholdNum.intValue() : 0;
      boolean shouldUnlock = false;

      if ("QUEST_COUNT".equals(type)) {
        shouldUnlock = questCompletedCount >= threshold;
      } else if ("TASK_COUNT".equals(type)) {
        shouldUnlock = taskCompletedCount >= threshold;
      } else if ("DOMAIN_TASK_COUNT".equals(type)) {
        shouldUnlock = domainTaskCounts.values().stream().anyMatch(c -> c >= threshold);
      }

      if (shouldUnlock) {
        UserAchievement ua = UserAchievement.create(userId, ach.getId());
        userAchievementRepository.save(ua);
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId.toString());
        payload.put("achievementId", ach.getId().toString());
        payload.put("code", ach.getCode());
        payload.put("title", ach.getTitle());
        outboxPublisher.publish("World", userId, "achievement.unlocked", payload);
        log.info("Achievement unlocked userId={} code={} achievementId={}", userId, ach.getCode(), ach.getId());
      }
    }
  }
}
