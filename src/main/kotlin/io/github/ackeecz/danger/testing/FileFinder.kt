package io.github.ackeecz.danger.testing

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.name

internal object FileFinder {

    private const val BUILD_DIR_NAME = "build"

    fun findFiles(
        rootDirectoryPath: Path,
        buildFoldersMatcher: BuildFoldersMatcher,
        reportFilesFolderPath: String,
        filePrefix: String,
        fileExtension: String,
    ): List<File> {
        return getBuildFolders(buildFoldersMatcher, rootDirectoryPath)
            .flatMap { buildDir ->
                buildDir.findReportFiles(
                    reportFilesFolderPath = reportFilesFolderPath,
                    filePrefix = filePrefix,
                    fileExtension = fileExtension,
                )
            }
            .ifEmpty {
                throw NoFilesFoundException("No report files found. Check your configuration.")
            }
    }

    private fun getBuildFolders(
        matcher: BuildFoldersMatcher,
        rootDirectoryPath: Path,
    ): List<File> {
        return when (matcher) {
            is BuildFoldersMatcher.All -> {
                Files.find(
                    rootDirectoryPath,
                    MAX_FILE_FIND_DEPTH,
                    { path, attributes ->
                        attributes.isDirectory && path.fileName.name == BUILD_DIR_NAME
                    },
                ).map { it.toFile() }.toList()
            }
            is BuildFoldersMatcher.Specific -> {
                matcher.paths.map { File(rootDirectoryPath.toFile(), it) }
            }
        }
    }

    private fun File.findReportFiles(
        reportFilesFolderPath: String,
        filePrefix: String,
        fileExtension: String,
    ): List<File> {
        return runCatching {
            Files.find(
                Paths.get(absolutePath, reportFilesFolderPath),
                MAX_FILE_FIND_DEPTH,
                { path, _ ->
                    val file = path.toFile()
                    file.name.startsWith(filePrefix) && file.extension == fileExtension
                },
            ).map { it.toFile() }.toList()
        }.getOrElse { emptyList() }
    }
}
