// settings.gradle.kts

pluginManagement {
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
        // ESTA É A LINHA ESSENCIAL QUE FALTAVA
        // Adiciona o repositório JitPack para que o Gradle possa encontrar a biblioteca Geohash
        maven { url = uri("https://jitpack.io" ) }
    }
}

rootProject.name = "MeshWave-Core"
include(":app")
