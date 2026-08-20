package com.questhub.modules.quest.domain.skilldomain;

import java.util.UUID;

public class SkillDomain {

  private final UUID id;
  private final String name;
  private final String slug;

  private SkillDomain(UUID id, String name, String slug) {
    this.id = id;
    this.name = name;
    this.slug = slug;
  }

  public static SkillDomain create(String name, String slug) {
    return new SkillDomain(UUID.randomUUID(), name, slug);
  }

  public static SkillDomain restore(UUID id, String name, String slug) {
    return new SkillDomain(id, name, slug);
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getSlug() {
    return slug;
  }
}