@org.springframework.modulith.ApplicationModule(
    id = "marketplace",
    displayName = "marketplace",
    allowedDependencies = {"shared", "quest::dto", "quest::api"}
)
package com.questhub.modules.marketplace;
