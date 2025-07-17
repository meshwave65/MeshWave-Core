// app/build.gradle.kts
// VERSÃO FINAL E CORRIGIDA

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.meshwave.core"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.meshwave.core"
        minSdk = 26
        targetSdk = 34

        // --- Lógica de Versionamento Dinâmico ---
        val major = project.property("APP_VERSION_MAJOR").toString().toInt()
        val minor = project.property("APP_VERSION_MINOR").toString().toInt()
        val patch = project.property("APP_VERSION_PATCH").toString().toInt()
        val build = project.property("APP_VERSION_BUILD").toString().toInt()
        val suffix = project.property("APP_VERSION_SUFFIX").toString()

        versionCode = build
        if (suffix.isNotEmpty() && suffix != "none") {
            versionName = "$major.$minor.$patch-$suffix"
        } else {
            versionName = "$major.$minor.$patch"
        }
        // --- Fim da Lógica de Versionamento ---

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("ch.hsr:geohash:1.4.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
}
