@org.springframework.modulith.ApplicationModule(
    id = "world",
    displayName = "world",
    allowedDependencies = {"shared", "quest::dto", "quest::api", "identity::api"}
)
package com.questhub.modules.world;
