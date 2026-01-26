package io.github.ackeecz.danger.testing.junit

import io.github.ackeecz.danger.testing.FileFinder
import systems.danger.kotlin.sdk.DangerContext
import java.io.File
import java.nio.file.Path

internal class JUnitResultFileFinder(private val context: DangerContext) {

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
        ).also {
            if (it.isEmpty()) {
                context.warn("No JUnit test results found")
            }
        }
    }

    companion object {

        private const val RESULT_FILE_EXTENSION = "xml"
    }
}
