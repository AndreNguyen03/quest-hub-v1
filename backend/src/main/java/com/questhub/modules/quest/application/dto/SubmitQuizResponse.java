package com.questhub.modules.quest.application.dto;

import java.util.UUID;

public record SubmitQuizResponse(UUID attemptId, boolean passed, boolean taskCompleted) {}
