package com.questhub.modules.admin.application.event;

import com.questhub.modules.admin.domain.skilldomain.AdminSkillDomain;
import com.questhub.shared.outbox.OutboxPublisher;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SkillDomainUpdatedEventPublisher {

  private final OutboxPublisher outboxPublisher;

  public void publish(AdminSkillDomain domain) {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("skillDomainId", domain.getId().toString());
    payload.put("name", domain.getName());
    payload.put("slug", domain.getSlug());
    payload.put("description", domain.getDescription());
    payload.put("icon", domain.getIcon());
    payload.put("isActive", domain.isActive());

    outboxPublisher.publish("SkillDomain", domain.getId(), "skill_domain.updated", payload);
  }
}
