package com.questhub.shared.outbox;

import java.util.Map;
import java.util.UUID;

public record OutboxEventDispatched(UUID eventId, String eventType, Map<String, Object> payload) {}