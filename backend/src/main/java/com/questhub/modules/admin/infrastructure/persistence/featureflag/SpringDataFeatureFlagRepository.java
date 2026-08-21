package com.questhub.modules.admin.infrastructure.persistence.featureflag;

import java.util.Map;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataFeatureFlagRepository extends JpaRepository<FeatureFlagJpaEntity, String> {

  Optional<FeatureFlagJpaEntity> findById(String key);
}
