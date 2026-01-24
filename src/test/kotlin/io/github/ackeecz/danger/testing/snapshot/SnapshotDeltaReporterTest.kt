package io.github.ackeecz.danger.testing.snapshot

import io.github.ackeecz.danger.testing.FakeDangerContext
import io.github.ackeecz.danger.testing.MarkdownHeader
import io.github.ackeecz.danger.testing.util.rootTempTestDir
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import java.io.File

private lateinit var dangerContext: FakeDangerContext
private lateinit var underTest: SnapshotDeltaReporter

internal class SnapshotDeltaReporterTest : FunSpec({

    beforeEach {
        dangerContext = FakeDangerContext()
        underTest = SnapshotDeltaReporter(dangerContext)
    }

    fun setUpConfig(
        host: String = "gitlab.custom.com",
        projectGroupName: String = "my-group",
        projectName: String = "my-project",
        snapshotJobIdFilePath: String = "${rootTempTestDir.absolutePath}/file.txt",
        failedSnapshotsMarkdownHeader: MarkdownHeader = MarkdownHeader(MarkdownHeader.Level.H3, "Failed snapshots"),
    ): SnapshotConfig.DeltaReport {
        val file = File(snapshotJobIdFilePath)
        if (!file.exists()) {
            file.createNewFile()
        }
        return SnapshotConfig.DeltaReport(
            host = host,
            projectGroupName = projectGroupName,
            projectName = projectName,
            snapshotJobIdFilePath = snapshotJobIdFilePath,
            failedSnapshotsHeader = failedSnapshotsMarkdownHeader,
        )
    }

    test("report failed delta snapshots with correct URL") {
        val deltaFiles = listOf(
            File("/path/to/delta-1.png"),
            File("/path/to/delta-2.png"),
        )

        val config = setUpConfig(
            host = "gitlab.custom.com",
            projectGroupName = "my-group",
            projectName = "my-project",
            snapshotJobIdFilePath = File(rootTempTestDir, "job-id.txt").also { it.writeText("123") }.absolutePath,
        )
        val expectedMessages = deltaFiles.map { file ->
            val jobPath = "${config.snapshotJobId}/artifacts/raw${file.absolutePath}"
            "![${file.name}](https://${config.host}/${config.projectGroupName}/${config.projectName}/-/jobs/$jobPath)"
        }

        underTest.report(deltaFiles, config)

        dangerContext.markdowns
            .drop(1) // Heading
            .map { it.message }
            .shouldContainExactlyInAnyOrder(expectedMessages)
    }

    test("report header when deltas found") {
        val deltaFiles = listOf(File("path/to/delta-1.png"))
        val header = MarkdownHeader(MarkdownHeader.Level.H1, "Custom header")
        val expectedHeader = "# ${header.text}"
        val config = setUpConfig(failedSnapshotsMarkdownHeader = header)

        underTest.report(deltaFiles, config)

        dangerContext.markdowns.first().message shouldBe expectedHeader
    }

    test("do not report anything for empty files") {
        val deltaFiles = emptyList<File>()
        val config = setUpConfig()

        underTest.report(deltaFiles, config)

        dangerContext.markdowns.shouldBeEmpty()
    }
})
