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

        val counters = doc.getElementsByTagName("counter")

        for (i in 0 until counters.length) {
            val node = counters.item(i)
            if (node.attributes.getNamedItem("type").nodeValue == "LINE") {
                val missed = node.attributes.getNamedItem("missed").nodeValue.toLong()
                val covered = node.attributes.getNamedItem("covered").nodeValue.toLong()

                val percent = (covered * 100.0) / (missed + covered)
                println("%.1f".format(percent))
                break
            }
        }
    }
}
