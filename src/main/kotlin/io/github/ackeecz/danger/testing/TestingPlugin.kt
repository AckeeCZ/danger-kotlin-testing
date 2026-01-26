package io.github.ackeecz.danger.testing

import io.github.ackeecz.danger.testing.junit.JUnitConfig
import io.github.ackeecz.danger.testing.junit.JUnitReportProcessor
import io.github.ackeecz.danger.testing.junit.JUnitResultFileFinder
import io.github.ackeecz.danger.testing.snapshot.SnapshotConfig
import io.github.ackeecz.danger.testing.snapshot.SnapshotDeltaReporter
import io.github.ackeecz.danger.testing.snapshot.SnapshotReportFileFinder
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
        val reportFiles = JUnitResultFileFinder(context).findFiles(
            rootDirectoryPath = Paths.get(""),
            config = config.discovery,
        )
        JUnitReportProcessor(context).process(reportFiles)
    }

    /**
     * Finds and processes failed snapshot delta image files and reports them to the pull request.
     * It is necessary to call this method only from the GitLab CI pipeline.
     *
     * @param config Config of snapshots processing
     */
    public fun findAndProcessSnapshotReports(config: SnapshotConfig = SnapshotConfig()) {
        val failedDeltaFiles = SnapshotReportFileFinder.findFiles(
            rootDirectoryPath = Paths.get(""),
            config = config.deltaDiscovery,
        )
        SnapshotDeltaReporter(context).report(failedDeltaFiles, config.deltaReport)
    }
}
