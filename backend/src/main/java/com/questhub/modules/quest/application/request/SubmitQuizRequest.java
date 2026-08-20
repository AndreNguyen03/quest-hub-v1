package com.questhub.modules.quest.application.request;

import java.util.Map;

public record SubmitQuizRequest(Map<String, Object> answers) {}