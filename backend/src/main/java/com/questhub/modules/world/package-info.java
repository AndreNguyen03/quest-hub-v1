@org.springframework.modulith.ApplicationModule(
    id = "world",
    displayName = "world",
    allowedDependencies = {"shared", "quest::dto", "quest::query", "identity::query"}
)
package com.questhub.modules.world;
