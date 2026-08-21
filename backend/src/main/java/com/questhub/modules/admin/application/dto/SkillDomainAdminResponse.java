package com.questhub.modules.admin.application.dto;

import com.questhub.modules.admin.domain.skilldomain.AdminSkillDomain;
import java.time.Instant;
import java.util.UUID;

public record SkillDomainAdminResponse(
    UUID id,
    String name,
    String slug,
    String description,
    String icon,
    boolean isActive,
    Instant createdAt,
    Instant updatedAt) {

  public static SkillDomainAdminResponse from(AdminSkillDomain domain) {
    return new SkillDomainAdminResponse(
        domain.getId(),
        domain.getName(),
        domain.getSlug(),
        domain.getDescription(),
        domain.getIcon(),
        domain.isActive(),
        domain.getCreatedAt(),
        domain.getUpdatedAt());
  }
}
