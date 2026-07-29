import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

abstract class VerifyReleaseAudioResourcesTask : DefaultTask() {

    @get:InputFile
    abstract val shrinkReport: RegularFileProperty

    @TaskAction
    fun verifyResources() {
        val reportFile = shrinkReport.get().asFile
        check(reportFile.isFile) {
            "Release resource shrink report was not generated at ${reportFile.absolutePath}"
        }

        val reportLines = reportFile.readLines()
        listOf("beep", "ding").forEach { resourceName ->
            val resourceLines = reportLines.filter { line -> line.contains("raw:$resourceName:") }
            check(resourceLines.isNotEmpty()) {
                "Release resource shrink report does not contain raw/$resourceName"
            }
            check(resourceLines.none { line -> line.contains("is not reachable") }) {
                "Release resource shrinking removed raw/$resourceName"
            }
        }
    }
}

val demoAdUnitId = "demo-banner-yandex"
val releaseAchievementsAdUnitId = "R-M-19603758-1"
val releaseGardenAdUnitId = "R-M-19603758-2"

val blinklyVersionName = providers.gradleProperty("blinklyVersionName")
    .getOrElse("1.0.0")
val blinklyVersionCode = providers.gradleProperty("blinklyVersionCode")
    .getOrElse("1")
    .toInt()

val releaseSigningValues = mapOf(
    "ANDROID_KEYSTORE_PATH" to providers.environmentVariable("ANDROID_KEYSTORE_PATH").orNull,
    "ANDROID_KEYSTORE_PASSWORD" to providers.environmentVariable("ANDROID_KEYSTORE_PASSWORD").orNull,
    "ANDROID_KEY_ALIAS" to providers.environmentVariable("ANDROID_KEY_ALIAS").orNull,
    "ANDROID_KEY_PASSWORD" to providers.environmentVariable("ANDROID_KEY_PASSWORD").orNull,
)
val releaseSigningConfigured = releaseSigningValues.values.any { !it.isNullOrBlank() }

check(Regex("^[0-9]+\\.[0-9]+\\.[0-9]+$").matches(blinklyVersionName)) {
    "blinklyVersionName '$blinklyVersionName' must be a stable SemVer value such as 0.1.0"
}
check(blinklyVersionCode in 1..2_100_000_000) {
    "blinklyVersionCode must be between 1 and 2100000000"
}
check(!releaseSigningConfigured || releaseSigningValues.values.all { !it.isNullOrBlank() }) {
    val missingValues = releaseSigningValues.filterValues { it.isNullOrBlank() }.keys.sorted()
    "Incomplete Android release signing configuration. Missing: ${missingValues.joinToString()}"
}

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
        versionCode = blinklyVersionCode
        versionName = blinklyVersionName
    }

    signingConfigs {
        if (releaseSigningConfigured) {
            create("release") {
                storeFile = file(releaseSigningValues.getValue("ANDROID_KEYSTORE_PATH")!!)
                storePassword = releaseSigningValues.getValue("ANDROID_KEYSTORE_PASSWORD")
                keyAlias = releaseSigningValues.getValue("ANDROID_KEY_ALIAS")
                keyPassword = releaseSigningValues.getValue("ANDROID_KEY_PASSWORD")
            }
        }
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
            if (releaseSigningConfigured) {
                signingConfig = signingConfigs.getByName("release")
            }

            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )

            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }

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

val verifyReleaseAudioResources by tasks.registering(VerifyReleaseAudioResourcesTask::class) {
    group = "verification"
    description = "Verifies that release resource shrinking keeps Blinkly audio resources."
    dependsOn("minifyReleaseWithR8")
    shrinkReport.set(layout.buildDirectory.file("outputs/mapping/release/resources.txt"))
}

tasks.matching { it.name == "preReleaseBuild" }.configureEach {
    dependsOn(verifyReleaseAdsConfiguration)
}

tasks.matching { it.name == "assembleRelease" || it.name == "bundleRelease" }.configureEach {
    finalizedBy(verifyReleaseAudioResources)
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
