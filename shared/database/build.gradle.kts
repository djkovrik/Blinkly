plugins {
    id("blinkly.config.android")
    id("blinkly.config.multiplatform")
    alias(libs.plugins.sqldelight)
}

sqldelight {
    databases {
        create("BlinklyAppDatabase") {
            packageName.set("com.sedsoftware.blinkly.database")
        }
    }
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":shared:domain"))

                implementation(libs.lib.sqldelight.coroutines)
                implementation(libs.lib.sqldelight.adapters)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.lib.sqldelight.driver.android)
                implementation(libs.lib.sqldelight.driver.sqlite)
            }
        }

        jvmMain {
            dependencies {
                implementation(libs.lib.sqldelight.driver.sqlite)
            }
        }

        iosMain {
            dependencies {
                implementation(libs.lib.sqldelight.driver.native)
            }
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
    }
}
