plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.jegly.offlineLLM"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.jegly.offlineLLM"
        minSdk = 33
        targetSdk = 37
        // CI release builds pass these; local builds use the defaults.
        versionCode = providers.gradleProperty("appVersionCode").map(String::toInt).getOrElse(11)
        versionName = providers.gradleProperty("appVersionName").getOrElse("5.1.3")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            abiFilters += "arm64-v8a"
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
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            // ggml discovers its backend plugins (libggml-cpu-android_*.so,
            // libggml-vulkan.so) by scanning nativeLibraryDir at runtime. With the
            // modern default (libs left compressed inside the APK) that directory
            // is empty and no backend can load — so extract them to disk.
            useLegacyPackaging = true
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

dependencies {
    // Native inference module
    implementation(project(":smollm"))

    // Compose BOM
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.animation)
    implementation(libs.compose.foundation)

    // Activity & Navigation
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)

    // Lifecycle
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.lifecycle.viewmodel.compose)

    // Room
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)

    // Coroutines
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // Security
    implementation(libs.security.crypto)
    implementation(libs.biometric)

    // Core
    implementation(libs.core.ktx)
    implementation(libs.core.splashscreen)

    // Serialization
    implementation(libs.serialization.json)

    // Markdown rendering
    implementation(libs.markdown.renderer.m3)
    implementation(libs.markdown.renderer.code)

    // Debug
    debugImplementation(libs.compose.ui.tooling)
}
