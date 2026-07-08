plugins {
    id("blinkly.config.android")
    id("blinkly.config.multiplatform")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":shared:domain"))

                implementation(libs.lib.multiplatform.settings.core)
                implementation(libs.lib.multiplatform.settings.test)
            }
        }
        androidMain {
            dependencies {
                implementation(libs.androidx.preferences)
            }
        }
    }
}
