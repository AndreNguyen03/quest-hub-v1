package com.questhub.modules.quest.application.dto;

import java.util.UUID;

public record LearningPathDto(UUID id, String title, String description, String difficulty, UUID domainId) {}
