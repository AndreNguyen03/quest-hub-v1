@org.springframework.modulith.ApplicationModule(
    id = "admin",
    displayName = "admin",
    allowedDependencies = {"shared", "quest", "world", "identity", "marketplace"}
)
package com.questhub.modules.admin;
