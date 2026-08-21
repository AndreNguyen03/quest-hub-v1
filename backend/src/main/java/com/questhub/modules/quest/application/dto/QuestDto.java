package com.questhub.modules.quest.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record QuestDto(UUID id, String title, String description, String difficulty, UUID learningPathId, int forkCount, BigDecimal avgRating) {}
