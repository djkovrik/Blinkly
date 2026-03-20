plugins {
    id("blinkly.config.android")
    id("blinkly.config.multiplatform")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":shared:domain"))
                implementation(project(":shared:alarm"))
                implementation(project(":shared:database"))
                implementation(project(":shared:notifier"))
                implementation(project(":shared:settings"))
                implementation(project(":shared:utils"))
                implementation(project(":shared:component:onboarding"))
                implementation(project(":shared:component:onboarding:child:step1"))
                implementation(project(":shared:component:onboarding:child:step2"))
                implementation(project(":shared:component:onboarding:child:step3"))
                implementation(project(":shared:component:onboarding:child:step4"))
                implementation(project(":shared:component:onboarding:child:step5"))
                implementation(project(":shared:component:home"))
                implementation(project(":shared:component:main"))
                implementation(project(":shared:component:main:child:preferences"))
                implementation(project(":shared:component:progress"))
                implementation(project(":shared:component:progress:child:achievements"))
                implementation(project(":shared:component:progress:child:garden"))
                implementation(project(":shared:component:reminders"))
                implementation(project(":shared:component:reminders:child:newreminder"))
                implementation(project(":shared:component:trainings"))
                implementation(project(":shared:component:trainings:child:blocka"))
                implementation(project(":shared:component:trainings:child:blockb"))
                implementation(project(":shared:component:trainings:child:blockc"))

                implementation(libs.ark.decompose.core)
                implementation(libs.ark.decompose.extensions)
                implementation(libs.ark.mvikotlin.core)
                implementation(libs.ark.mvikotlin.main)
                implementation(libs.ark.mvikotlin.extensions)
                implementation(libs.ark.essenty)

                implementation(libs.lib.moko.permissions)
                implementation(libs.lib.multiplatform.settings.core)
            }
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
    }
}
