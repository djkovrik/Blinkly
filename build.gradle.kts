plugins {
    alias(libs.plugins.kotlin.multiplatform).apply(false)
    alias(libs.plugins.compose.compiler).apply(false)
    alias(libs.plugins.compose.multiplatform).apply(false)
    alias(libs.plugins.kotlin.android).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.kmp.library).apply(false)
    alias(libs.plugins.kotlin.jvm).apply(false)
    alias(libs.plugins.kotlinx.serialization).apply(false)
    alias(libs.plugins.room).apply(false)
    alias(libs.plugins.ksp).apply(false)
    alias(libs.plugins.mokkery).apply(false)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
    alias(libs.plugins.coverage)
}

detekt {
    allRules = false
    parallel = true
    buildUponDefaultConfig = true
    baseline = file("$projectDir/detekt/baseline.xml")
    config.setFrom(file("$projectDir/detekt/base-config.yml"))
    source.setFrom(files("$projectDir/shared/"))
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        html.required.set(true)
        html.outputLocation.set(file("$projectDir/detekt/reports/detekt.html"))
    }
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    jvmTarget = "1.8"
    exclude("**/build/**")
}

tasks.withType<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>().configureEach {
    jvmTarget = "1.8"
    exclude("**/build/**")
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "com.sedsoftware.blinkly.*.integration.*Preview",
                    "com.sedsoftware.blinkly.*.*Module",
                )
            }
            includes {
                classes(
                    "com.sedsoftware.*.domain.*",
                    "com.sedsoftware.*.extension.*",
                    "com.sedsoftware.*.integration.*Default",
                    "com.sedsoftware.*.store.*",
                    "com.sedsoftware.*.mapper.*",
                )
            }
        }
    }
}

dependencies {
    rootProject.subprojects {
        if (!this.path.contains("root") && !this.path.contains("compose") && file("build.gradle.kts").exists()) {
            kover(this)
        }
    }

    detektPlugins(libs.lib.detekt.compose)
    detektPlugins(libs.lib.detekt.decompose)
}
