package com.questhub.modules.quest.application.request;

import java.util.Map;

public record CompleteTaskRequest(Map<String, Object> evidence) {}