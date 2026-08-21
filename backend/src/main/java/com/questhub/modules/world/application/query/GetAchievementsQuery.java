package com.questhub.modules.world.application.query;

import com.questhub.modules.world.domain.achievement.Achievement;
import com.questhub.modules.world.domain.achievement.AchievementRepository;
import com.questhub.modules.world.domain.achievement.UserAchievement;
import com.questhub.modules.world.domain.achievement.UserAchievementRepository;
import com.questhub.shared.annotation.UseCase;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class GetAchievementsQuery {

  private final AchievementRepository achievementRepository;
  private final UserAchievementRepository userAchievementRepository;

  public record AchievementDto(
      UUID id,
      String code,
      String title,
      String description,
      Map<String, Object> criteria,
      Instant unlockedAt,
      boolean unlocked) {}

  @Transactional(
      readOnly = true,
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public List<AchievementDto> get(UUID userId, boolean onlyLocked) {
    List<Achievement> all = achievementRepository.findAll();
    Map<UUID, Instant> unlockedMap =
        userAchievementRepository.findByUserId(userId).stream()
            .collect(Collectors.toMap(UserAchievement::getAchievementId, UserAchievement::getUnlockedAt));

    List<AchievementDto> result = new ArrayList<>();
    for (Achievement a : all) {
      Instant unlockedAt = unlockedMap.get(a.getId());
      boolean unlocked = unlockedAt != null;
      if (onlyLocked && unlocked) {
        continue;
      }
      if (!onlyLocked && !unlocked) {
        // if not onlyLocked, we return unlocked ones; but spec says onlyLocked=true to see locked, default to show unlocked
        // Actually per plan: GET /world/achievements returns unlocked, ?onlyLocked=true returns locked
        // So we need to handle: if onlyLocked=false, return only unlocked; if true, return only locked
        // Let's implement that
      }
      result.add(new AchievementDto(a.getId(), a.getCode(), a.getTitle(), a.getDescription(), a.getCriteria(), unlockedAt, unlocked));
    }

    // Filter according to spec
    if (onlyLocked) {
      return result.stream().filter(dto -> !dto.unlocked()).toList();
    } else {
      return result.stream().filter(AchievementDto::unlocked).toList();
    }
  }
}








