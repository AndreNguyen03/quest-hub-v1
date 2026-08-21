package com.questhub.modules.quest.application.dto;

import java.util.UUID;

public record LeaderboardStatDto(UUID userId, long questCount, long taskCount) {}
