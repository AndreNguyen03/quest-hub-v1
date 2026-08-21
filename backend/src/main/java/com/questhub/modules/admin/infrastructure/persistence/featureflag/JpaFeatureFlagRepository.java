package com.questhub.modules.admin.infrastructure.persistence.featureflag;

import com.questhub.modules.admin.domain.featureflag.FeatureFlag;
import com.questhub.modules.admin.domain.featureflag.FeatureFlagRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class JpaFeatureFlagRepository implements FeatureFlagRepository {

  private final SpringDataFeatureFlagRepository jpa;

  public JpaFeatureFlagRepository(SpringDataFeatureFlagRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public FeatureFlag save(FeatureFlag featureFlag) {
    FeatureFlagJpaEntity entity =
        new FeatureFlagJpaEntity(
            featureFlag.getKey(),
            featureFlag.getValue(),
            featureFlag.getDescription(),
            featureFlag.getUpdatedAt());
    jpa.save(entity);
    return featureFlag;
  }

  @Override
  public Optional<FeatureFlag> findById(String key) {
    return jpa.findById(key).map(FeatureFlagMapper::toDomain);
  }

  @Override
  public List<FeatureFlag> findAll() {
    return jpa.findAll().stream().map(FeatureFlagMapper::toDomain).collect(Collectors.toList());
  }
}
