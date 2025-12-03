package io.github.ackeecz.danger.testing.snapshot

import io.github.ackeecz.danger.testing.FileFinder
import java.io.File
import java.nio.file.Path

internal object SnapshotReportFileFinder {

    fun findFiles(
        rootDirectoryPath: Path,
        config: SnapshotConfig.DeltaDiscovery
    ): List<File> {
        return FileFinder.findFiles(
            rootDirectoryPath = rootDirectoryPath,
            buildFoldersMatcher = config.buildFoldersMatcher,
            reportFilesFolderPath = config.deltasFolderPath,
            filePrefix = config.deltaFileNamePrefix,
            fileExtension = config.deltaFileExtension,
        )
    }
}
