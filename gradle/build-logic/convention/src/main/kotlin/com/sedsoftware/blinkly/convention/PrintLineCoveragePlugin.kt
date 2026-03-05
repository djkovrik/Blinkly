package com.sedsoftware.blinkly.convention

import org.gradle.api.Plugin
import org.gradle.api.Project

class PrintLineCoveragePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register(
            "printLineCoverage",
            PrintLineCoverageTask::class.java
        ) {
            group = "verification"
            dependsOn("koverXmlReport")

            reportFile.set(
                project.layout.buildDirectory.file("reports/kover/report.xml")
            )
        }
    }
}
