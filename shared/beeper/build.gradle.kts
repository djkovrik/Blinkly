plugins {
    id("blinkly.config.android")
    id("blinkly.config.multiplatform")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":shared:domain"))
            }
        }
    }
}
