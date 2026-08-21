@org.springframework.modulith.ApplicationModule(
    id = "admin",
    displayName = "admin",
    allowedDependencies = {"shared", "quest::query", "quest::dto", "world::query", "identity::query", "marketplace::query"}
)
package com.questhub.modules.admin;
