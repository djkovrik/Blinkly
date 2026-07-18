import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

abstract class GenerateComposablePreviewPaparazziTestsTask : DefaultTask() {
    @get:Input
    abstract val previewPackages: ListProperty<String>

    @get:Input
    abstract val testPackageName: Property<String>

    @get:Input
    abstract val testClassName: Property<String>

    @get:Input
    abstract val compileSdkVersion: Property<Int>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val packageName = testPackageName.get()
        val className = testClassName.get()
        val packagePath = packageName.replace('.', '/')
        val outputFile = outputDirectory.file("$packagePath/$className.kt").get().asFile
        val previewPackageLiterals = previewPackages.get().joinToString(separator = ", ") { "\"$it\"" }
        val previewPackagePrefix = previewPackages.get().first()

        outputFile.parentFile.mkdirs()
        outputFile.writeText(
            """
            @file:Suppress("LongMethod")

            package $packageName

            import app.cash.paparazzi.Paparazzi
            import com.sedsoftware.blinkly.compose.ui.paparazzi.BlinklyPaparazziPreviewRule
            import com.sedsoftware.blinkly.compose.ui.paparazzi.BlinklyPreviewContent
            import com.sedsoftware.blinkly.compose.ui.paparazzi.configureComposeResources
            import org.junit.Rule
            import org.junit.Test
            import org.junit.runner.RunWith
            import org.junit.runners.Parameterized
            import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
            import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
            import sergio.sastre.composable.preview.scanner.android.screenshotid.AndroidPreviewScreenshotIdBuilder
            import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

            @RunWith(Parameterized::class)
            class $className(
                private val preview: ComposablePreview<AndroidPreviewInfo>,
            ) {
                companion object {
                    @JvmStatic
                    @Parameterized.Parameters(name = "{0}")
                    fun previews(): List<ComposablePreview<AndroidPreviewInfo>> =
                        AndroidComposablePreviewScanner()
                            .scanPackageTrees(
                                include = listOf($previewPackageLiterals),
                                exclude = emptyList(),
                            )
                            .includePrivatePreviews()
                            .getPreviews()
                }

                @get:Rule
                val paparazzi: Paparazzi = BlinklyPaparazziPreviewRule.createFor(
                    preview = preview,
                    compileSdkVersion = ${compileSdkVersion.get()},
                )

                @Test
                fun snapshot() {
                    paparazzi.configureComposeResources()

                    val screenshotId = AndroidPreviewScreenshotIdBuilder(preview)
                        .doNotIgnoreMethodParametersType()
                        .encodeUnsafeCharacters()
                        .build()
                        .replace("$previewPackagePrefix.", "")

                    paparazzi.snapshot(name = screenshotId) {
                        BlinklyPreviewContent(preview.previewInfo) {
                            preview()
                        }
                    }
                }
            }
            """.trimIndent()
        )
    }
}

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.paparazzi)
}

val paparazziPreviewPackage = "com.sedsoftware.blinkly.compose.ui"
val paparazziGeneratedTestPackage = "com.sedsoftware.blinkly.compose.ui.paparazzi.generated"
val paparazziGeneratedTestClass = "GeneratedComposablePreviewPaparazziTest"
val paparazziGeneratedTestDir = layout.buildDirectory.dir("generated/source/paparazziPreviews/androidUnitTest/kotlin")
val paparazziRenderCompileSdk = 35

val generateComposablePreviewPaparazziTests by tasks.registering(GenerateComposablePreviewPaparazziTestsTask::class) {
    group = "verification"
    description = "Generates Paparazzi screenshot tests for Compose @Preview functions."

    previewPackages.set(listOf(paparazziPreviewPackage))
    testPackageName.set(paparazziGeneratedTestPackage)
    testClassName.set(paparazziGeneratedTestClass)
    compileSdkVersion.set(paparazziRenderCompileSdk)
    outputDirectory.set(paparazziGeneratedTestDir)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    iosX64()
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
            implementation(libs.lib.yandex.mobileads.compose)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)

            implementation(libs.ark.decompose.core)
            implementation(libs.ark.decompose.extensions)

            implementation(project(":shared:domain"))
            implementation(project(":shared:utils"))
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
            implementation(project(":shared:component:trainings:child:workout"))
            implementation(project(":shared:component:sync"))
            implementation(libs.lib.kmpauth.google)
            implementation(libs.lib.kmpauth.firebase)
            implementation(libs.lib.kmpauth.uihelper)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.compose.ui.test)
            implementation(libs.test.kotlin.coroutines)
        }

        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
        }

        androidUnitTest {
            kotlin.srcDir(paparazziGeneratedTestDir)

            dependencies {
                implementation(libs.test.junit4)
                implementation(libs.test.composable.preview.scanner.android)
            }
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
        freeCompilerArgs.add("-opt-in=androidx.compose.material3.ExperimentalMaterial3Api")
    }
}

dependencies {
    debugImplementation(libs.compose.ui.tooling)
}

android {
    namespace = "com.sedsoftware.blinkly.compose"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.jvmArgs("-Xmx4g")
            }
        }
    }
}

tasks.matching {
    it.name == "compileDebugUnitTestKotlinAndroid" ||
        it.name == "compileReleaseUnitTestKotlinAndroid" ||
        it.name.startsWith("recordPaparazzi") ||
        it.name.startsWith("verifyPaparazzi")
}.configureEach {
    dependsOn(generateComposablePreviewPaparazziTests)
}
