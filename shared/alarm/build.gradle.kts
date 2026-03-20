plugins {
    id("blinkly.config.android")
    id("blinkly.config.multiplatform")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":shared:domain"))

                implementation(libs.compose.ui)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.lib.alarmee)
            }
        }

        androidUnitTest {
            dependencies {
                implementation(libs.lib.alarmee)
            }
        }

        iosArm64Main {
            dependencies {
                implementation(libs.lib.alarmee)
            }
        }

        iosSimulatorArm64Main {
            dependencies {
                implementation(libs.lib.alarmee)
            }
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.uuid.ExperimentalUuidApi")
        freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
    }
}
