plugins {
    id("blinkly.config.android")
    id("blinkly.config.multiplatform")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":shared:domain"))

                implementation(libs.lib.moko.permissions)
                implementation(libs.lib.moko.permissions.notifications)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.lib.moko.permissions.test)
            }
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi")
    }
}
