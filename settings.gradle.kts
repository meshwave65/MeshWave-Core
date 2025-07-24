pluginManagement {
    plugins {
        // Define a versão para AMBOS os plugins do Kotlin.
        // A versão deve ser a mesma do compilador principal.
        id("org.jetbrains.kotlin.plugin.serialization") version "2.2.0"
        id("org.jetbrains.kotlin.plugin.compose") version "2.2.0"
    }
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MeshWave-Core"
include(":app")
