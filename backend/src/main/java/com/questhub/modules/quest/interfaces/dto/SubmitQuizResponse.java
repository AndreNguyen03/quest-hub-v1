package com.questhub.modules.quest.interfaces.dto;

import java.util.UUID;

public record SubmitQuizResponse(UUID attemptId, boolean passed, boolean taskCompleted) {}