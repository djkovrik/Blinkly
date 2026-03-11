package com.sedsoftware.blinkly.convention

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import javax.xml.parsers.DocumentBuilderFactory

// Src: https://bitspittle.dev/blog/2022/kover-badge
abstract class PrintLineCoverageTask : DefaultTask() {

    @get:InputFile
    abstract val reportFile: RegularFileProperty

    @TaskAction
    fun printCoverage() {
        val doc = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(reportFile.get().asFile)

        val rootNode = doc.firstChild
        var childNode = rootNode.firstChild

        var coveragePercent = 0.0

        while (childNode != null) {
            if (childNode.nodeName == "counter") {
                val typeAttr = childNode.attributes.getNamedItem("type")
                if (typeAttr.textContent == "LINE") {
                    val missedAttr = childNode.attributes.getNamedItem("missed")
                    val coveredAttr = childNode.attributes.getNamedItem("covered")
                    val missed = missedAttr.textContent.toLong()
                    val covered = coveredAttr.textContent.toLong()
                    coveragePercent = (covered * HUNDRED_PERCENT) / (missed + covered)
                    break
                }
            }
            childNode = childNode.nextSibling
        }

        println("%.1f".format(coveragePercent))
    }

    private companion object {
        const val HUNDRED_PERCENT = 100.0
    }
}
