import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kover)
}

android {
    namespace = "com.sedsoftware.blinkly"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()

        applicationId = "com.sedsoftware.blinkly"
        versionCode = 1
        versionName = "1.0.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_21) }
}

dependencies {
    implementation(project(":shared:component:root"))
    implementation(project(":shared:compose"))
    implementation(project(":shared:domain"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.lib.alarmee)
    implementation(libs.lib.moko.permissions)
    implementation(libs.ark.decompose.core)
    implementation(libs.ark.decompose.extensions)
}
