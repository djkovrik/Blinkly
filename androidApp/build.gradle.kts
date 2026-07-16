import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val demoAdUnitId = "demo-banner-yandex"
val releaseAchievementsAdUnitId = "R-M-19603758-1"
val releaseGardenAdUnitId = "R-M-19603758-2"

fun quotedBuildConfigValue(value: String): String = "\"$value\""

plugins {
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
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

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            buildConfigField(
                type = "String",
                name = "BLINKLY_ACHIEVEMENTS_AD_UNIT_ID",
                value = quotedBuildConfigValue(demoAdUnitId),
            )
            buildConfigField(
                type = "String",
                name = "BLINKLY_GARDEN_AD_UNIT_ID",
                value = quotedBuildConfigValue(demoAdUnitId),
            )
        }
        release {
            buildConfigField(
                type = "String",
                name = "BLINKLY_ACHIEVEMENTS_AD_UNIT_ID",
                value = quotedBuildConfigValue(releaseAchievementsAdUnitId),
            )
            buildConfigField(
                type = "String",
                name = "BLINKLY_GARDEN_AD_UNIT_ID",
                value = quotedBuildConfigValue(releaseGardenAdUnitId),
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

val verifyReleaseAdsConfiguration by tasks.registering {
    group = "verification"
    description = "Verifies that enabled release ad placements use production Yandex ad unit IDs."
    inputs.properties(
        "achievementsAdUnitId" to releaseAchievementsAdUnitId,
        "gardenAdUnitId" to releaseGardenAdUnitId,
    )

    doLast {
        val configuredAdUnitIds = mapOf(
            "Achievements" to inputs.properties.getValue("achievementsAdUnitId").toString(),
            "Garden" to inputs.properties.getValue("gardenAdUnitId").toString(),
        )

        configuredAdUnitIds.forEach { (placement, adUnitId) ->
            check(adUnitId.isNotBlank()) { "$placement release ad unit ID is blank" }
            check(adUnitId != "demo-banner-yandex") { "$placement release ad unit ID uses the Yandex demo value" }
            check(adUnitId.startsWith("R-M-")) { "$placement release ad unit ID is not a Yandex production ID" }
        }
    }
}

tasks.matching { it.name == "preReleaseBuild" }.configureEach {
    dependsOn(verifyReleaseAdsConfiguration)
}

kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_21) }
}

dependencies {
    implementation(project(":shared:component:root"))
    implementation(project(":shared:compose"))
    implementation(project(":shared:domain"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splash)
    implementation(libs.lib.alarmee)
    implementation(libs.lib.kermit)
    implementation(libs.lib.moko.permissions)
    implementation(libs.ark.decompose.core)
    implementation(libs.ark.decompose.extensions)
}
