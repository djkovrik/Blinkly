plugins {
    id("blinkly.config.android")
    id("blinkly.config.multiplatform")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":shared:domain"))

                implementation(project(":shared:component:onboarding:child:step1"))
                implementation(project(":shared:component:onboarding:child:step2"))
                implementation(project(":shared:component:onboarding:child:step3"))
                implementation(project(":shared:component:onboarding:child:step4"))
                implementation(project(":shared:component:onboarding:child:step5"))

                implementation(libs.ark.decompose.core)
                implementation(libs.ark.decompose.extensions)
                implementation(libs.ark.mvikotlin.core)
                implementation(libs.ark.mvikotlin.main)
                implementation(libs.ark.mvikotlin.extensions)
                implementation(libs.ark.essenty)
            }
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-opt-in=com.arkivanov.decompose.DelicateDecomposeApi")
    }
}
