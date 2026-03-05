plugins {
    `kotlin-dsl`
}

group = "com.sedsoftware.blinkly.convention"

dependencies {
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("configureAndroid") {
            id = "blinkly.config.android"
            implementationClass = "com.sedsoftware.blinkly.convention.ConfigureAndroidPlugin"
        }
        register("configureMultiplatform") {
            id = "blinkly.config.multiplatform"
            implementationClass = "com.sedsoftware.blinkly.convention.ConfigureMultiplatformPlugin"
        }
        register("printLineCoverage") {
            id = "blinkly.config.printcoverage"
            implementationClass = "com.sedsoftware.blinkly.convention.PrintLineCoveragePlugin"
        }
    }
}
