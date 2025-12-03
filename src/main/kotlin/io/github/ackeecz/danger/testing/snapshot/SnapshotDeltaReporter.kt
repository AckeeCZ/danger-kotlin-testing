package io.github.ackeecz.danger.testing.snapshot

import systems.danger.kotlin.sdk.DangerContext
import java.io.File

internal class SnapshotDeltaReporter(
    private val dangerContext: DangerContext,
) {

    fun report(
        deltaFiles: List<File>,
        config: SnapshotConfig.DeltaReport,
    ) {
        if (deltaFiles.isNotEmpty()) {
            dangerContext.markdown(config.failedSnapshotsHeader.toMarkdownString())
            deltaFiles.forEach { file ->
                val groupName = config.projectGroupName
                val projectName = config.projectName
                val snapshotJobId = config.snapshotJobId
                val filePath = file.absolutePath.substringAfter(projectName)
                dangerContext.markdown(
                    "![${file.name}](https://${config.host}/$groupName/$projectName/-/jobs/$snapshotJobId/artifacts/raw$filePath)"
                )
            }
        }
    }
}
