plugins {
    id("blinkly.config.android")
    id("blinkly.config.multiplatform")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":shared:domain"))
                implementation(project(":shared:utils"))

                implementation(libs.ark.decompose.core)
                implementation(libs.ark.mvikotlin.core)
                implementation(libs.ark.mvikotlin.main)
                implementation(libs.ark.mvikotlin.extensions)
                implementation(libs.ark.essenty)
                implementation(libs.lib.firebase.app)
                implementation(libs.lib.firebase.auth)
                implementation(libs.lib.firebase.firestore)
                implementation(libs.kotlinx.serialization.json)
            }
        }
        androidMain {
            dependencies {
                implementation(project.dependencies.platform(libs.lib.firebase.bom))
            }
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
    }
}
