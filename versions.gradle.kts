import org.gradle.api.JavaVersion

object Versions {
    const val compileSdk = 34
    const val minSdk = 26
    const val targetSdk = 34
    const val versionCode = 1
    const val versionName = "0.3.2-alpha"

    val jvmTarget = JavaVersion.VERSION_1_8

    // Plugins
    const val androidGradlePlugin = "8.2.0"
    const val kotlinAndroidPlugin = "1.9.0"

    // AndroidX
    const val coreKtx = "1.12.0"
    const val appCompat = "1.6.1"
    const val material = "1.11.0"
    const val constraintLayout = "2.1.4"

    // Coroutines
    const val coroutines = "1.7.3"

    // Google Play Services
    const val playServicesLocation = "21.2.0"

    // GeoHash
    const val geohash = "1.4.0"

    // Testing
    const val junit = "4.13.2"
    const val androidxJunit = "1.1.5"
    const val espressoCore = "3.5.1"
}

object BuildPlugins {
    const val androidApplication = "com.android.application"
    const val kotlinAndroid = "org.jetbrains.kotlin.android"
}

object AndroidX {
    const val coreKtx = "androidx.core:core-ktx:${Versions.coreKtx}"
    const val appCompat = "androidx.appcompat:appcompat:${Versions.appCompat}"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"
}

object Google {
    const val material = "com.google.android.material:material:${Versions.material}"
    const val playServicesLocation = "com.google.android.gms:play-services-location:${Versions.playServicesLocation}"
}

object Kotlin {
    const val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
    const val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"
}

object ThirdParty {
    const val geohash = "ch.hsr:geohash:${Versions.geohash}"
}

object TestLibs {
    const val junit = "junit:junit:${Versions.junit}"
    const val androidxJunit = "androidx.test.ext:junit:${Versions.androidxJunit}"
    const val espressoCore = "androidx.test.espresso:espresso-core:${Versions.espressoCore}"
}


