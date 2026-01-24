package io.github.ackeecz.danger.testing

import io.github.ackeecz.danger.testing.junit.JUnitConfig
import io.github.ackeecz.danger.testing.junit.JUnitReportProcessor
import io.github.ackeecz.danger.testing.junit.JUnitResultFileFinder
import systems.danger.kotlin.sdk.DangerPlugin
import java.nio.file.Paths

/**
 * Danger-kotlin plugin for processing test reports and printing failures to the PR comment.
 */
public object TestingPlugin : DangerPlugin() {

    override val id: String = "danger-kotlin-testing"

    /**
     * Finds and parses XML outputs of JUnit tests and reports failures to the pull request.
     *
     * @param config Config of report processing
     */
    public fun findAndProcessJUnitReports(config: JUnitConfig = JUnitConfig()) {
        val reportFiles = JUnitResultFileFinder.findFiles(
            rootDirectoryPath = Paths.get(""),
            config = config.discovery,
        )
        JUnitReportProcessor(context).process(reportFiles)
    }
}
