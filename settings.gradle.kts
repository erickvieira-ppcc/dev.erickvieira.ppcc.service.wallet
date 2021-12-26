rootProject.name = "wallet-service"

pluginManagement {
    val dependencyManagementVersion: String by settings
    val pitestVersion: String by settings
    val openApiGeneratorVersion: String by settings
    val springBootVersion: String by settings
    plugins {
        id("io.spring.dependency-management") version dependencyManagementVersion
        id("info.solidsoft.pitest") version pitestVersion
        id("org.openapi.generator") version openApiGeneratorVersion
        id("org.springframework.boot") version springBootVersion
        kotlin("jvm") version "1.6.0"
        kotlin("plugin.spring") version "1.6.0"
    }
}