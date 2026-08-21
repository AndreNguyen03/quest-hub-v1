package com.questhub.modules.admin.domain.featureflag;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface FeatureFlagRepository {

  FeatureFlag save(FeatureFlag featureFlag);

  Optional<FeatureFlag> findById(String key);

  List<FeatureFlag> findAll();
}
