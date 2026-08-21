package com.questhub.modules.world.application.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AchievementResponse(
    UUID id,
    String code,
    String title,
    String description,
    Map<String, Object> criteria,
    Instant unlockedAt,
    boolean unlocked) {}


