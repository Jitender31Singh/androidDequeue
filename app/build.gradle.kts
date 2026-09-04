/**
 * App module build configuration.
 *
 * Architecture decisions:
 * - Uses KSP instead of kapt for Hilt and Room annotation processing (faster builds).
 * - Compose compiler is configured via the compose-compiler plugin (Kotlin 2.0+).
 * - All dependency versions are managed via gradle/libs.versions.toml.
 * - compileSdk 35 / minSdk 26 / targetSdk 35 for modern API coverage.
 */
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.services)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.appvendor"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.appvendor"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // ── Core Android ──────────────────────────────────────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation("androidx.print:print:1.0.0")

    // ── Compose (BOM manages versions) ────────────────────────────────────
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // ── Navigation ────────────────────────────────────────────────────────
    implementation(libs.androidx.navigation.compose)

    // ── Hilt (Dependency Injection) ───────────────────────────────────────
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // ── Retrofit + OkHttp (Networking) ────────────────────────────────────
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // ── Room (Local Database) ─────────────────────────────────────────────
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // ── DataStore (Preferences) ───────────────────────────────────────────
    implementation(libs.datastore.preferences)

    // ── Coroutines ────────────────────────────────────────────────────────
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // ── Firebase ──────────────────────────────────────────────────────────
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.messaging)
    implementation(libs.play.services.auth)
    implementation(libs.credential.manager)
    implementation(libs.credential.manager.play.services.auth)

    // ── QR Code (ZXing) ───────────────────────────────────────────────────
    implementation(libs.zxing.core)

    // ── Image Loading ─────────────────────────────────────────────────────
    implementation(libs.coil.compose)

    // ── Accompanist ───────────────────────────────────────────────────────
    implementation(libs.accompanist.systemuicontroller)

    // ── Kotlin Serialization (for type-safe navigation) ───────────────────
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // ── OpenStreetMap & Location ──────────────────────────────────────────
    implementation(libs.play.services.location)
    implementation("org.osmdroid:osmdroid-android:6.1.18")

    // ── WebSockets / STOMP ────────────────────────────────────────────────
    implementation("com.github.NaikSoftware:StompProtocolAndroid:1.6.6")
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")

    // ── Testing ───────────────────────────────────────────────────────────
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
}
