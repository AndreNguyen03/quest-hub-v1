@org.springframework.modulith.ApplicationModule(
    id = "marketplace",
    displayName = "marketplace",
    allowedDependencies = {"shared", "quest::dto", "quest::query", "identity::query"}
)
package com.questhub.modules.marketplace;
