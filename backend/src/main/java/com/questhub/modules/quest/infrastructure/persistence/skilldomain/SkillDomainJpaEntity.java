package com.questhub.modules.quest.infrastructure.persistence.skilldomain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "skill_domains")
public class SkillDomainJpaEntity {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "slug", nullable = false, length = 100)
  private String slug;

  protected SkillDomainJpaEntity() {}

  public SkillDomainJpaEntity(UUID id, String name, String slug) {
    this.id = id;
    this.name = name;
    this.slug = slug;
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


