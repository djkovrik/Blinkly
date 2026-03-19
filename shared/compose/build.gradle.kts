import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.android.kmp.library)
}

kotlin {
    androidTarget()

    iosArm64()
    iosSimulatorArm64()

    jvmToolchain(21)

    sourceSets {
        commonMain.dependencies {
            api(libs.compose.runtime)
            api(libs.compose.ui)
            api(libs.compose.foundation)
            api(libs.compose.resources)
            api(libs.compose.ui.tooling.preview)
            api(libs.compose.material3)
            implementation(libs.lib.kermit)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)

            implementation(libs.ark.decompose.core)
            implementation(libs.ark.decompose.extensions)

            implementation(project(":shared:domain"))
            implementation(project(":shared:component:root"))
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
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.compose.ui.test)
            implementation(libs.test.kotlin.coroutines)
        }

        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
        }
    }

    targets
        .withType<KotlinNativeTarget>()
        .matching { it.konanTarget.family.isAppleFamily }
        .configureEach {
            binaries {
                framework {
                    baseName = "compose"
                    isStatic = true
                }
            }
        }

    compilerOptions {
        freeCompilerArgs.add("-opt-in=com.arkivanov.decompose.ExperimentalDecomposeApi")
    }
}

dependencies {
    debugImplementation(libs.compose.ui.tooling)
}

android {
    namespace = "com.sedsoftware.blinkly.compose"
    compileSdk = 36
    defaultConfig {
        minSdk = 23
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}
