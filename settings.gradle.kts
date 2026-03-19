rootProject.name = "Blinkly"

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
                includeGroupByRegex("android.*")
            }
        }
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
                includeGroupByRegex("android.*")
            }
        }
        mavenCentral()
    }
}
include(
    ":androidApp",
    ":shared:compose",
    ":shared:alarm",
    ":shared:database",
    ":shared:domain",
    ":shared:notifier",
    ":shared:settings",
    ":shared:utils",
    ":shared:component:root",
    ":shared:component:onboarding",
    ":shared:component:onboarding:child:step1",
    ":shared:component:onboarding:child:step2",
    ":shared:component:onboarding:child:step3",
    ":shared:component:onboarding:child:step4",
    ":shared:component:onboarding:child:step5",
    ":shared:component:home",
    ":shared:component:main",
    ":shared:component:main:child:preferences",
    ":shared:component:progress",
    ":shared:component:progress:child:achievements",
    ":shared:component:progress:child:garden",
    ":shared:component:reminders",
    ":shared:component:reminders:child:newreminder",
    ":shared:component:trainings",
    ":shared:component:trainings:child:blocka",
    ":shared:component:trainings:child:blockb",
    ":shared:component:trainings:child:blockc",
)

includeBuild("gradle/build-logic")
