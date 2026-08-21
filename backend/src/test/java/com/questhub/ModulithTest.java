package com.questhub;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModulithTest {

  @Test
  void verifyModularity() {
    ApplicationModules modules = ApplicationModules.of(QuestHubApplication.class);
    // Verify that direct dependencies between modules are explicitly allowed via package-info
    modules.verify();
  }

  @Test
  void writeDocumentation() {
    ApplicationModules modules = ApplicationModules.of(QuestHubApplication.class);
    new Documenter(modules).writeDocumentation();
  }

  @Test
  void verifyModulesIndividually() {
    // Also verify that sub-modules under com.questhub.modules are correctly detected when using base package
    ApplicationModules modules = ApplicationModules.of("com.questhub");
    modules.verify();
  }
}
