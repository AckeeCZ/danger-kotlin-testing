package io.github.ackeecz.danger.testing.junit

import io.github.ackeecz.danger.testing.FileFinder
import java.io.File
import java.nio.file.Path

internal object JUnitResultFileFinder {

    private const val RESULT_FILE_EXTENSION = "xml"

    fun findFiles(
        rootDirectoryPath: Path,
        config: JUnitConfig.Discovery,
    ): List<File> {
        return FileFinder.findFiles(
            rootDirectoryPath = rootDirectoryPath,
            buildFoldersMatcher = config.buildFoldersMatcher,
            reportFilesFolderPath = config.testResultsFolderPath,
            filePrefix = config.resultFileNamePrefix,
            fileExtension = RESULT_FILE_EXTENSION,
        )
    }
}
