package io.github.ackeecz.danger.testing.snapshot

import io.github.ackeecz.danger.testing.BuildFoldersMatcher
import io.github.ackeecz.danger.testing.NoFilesFoundException
import io.github.ackeecz.danger.testing.util.rootTempTestDir
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.TestConfiguration
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import java.io.File
import java.nio.file.Paths

private const val DEFAULT_DELTAS_FOLDER = "paparazzi"
private const val DEFAULT_DELTA_PREFIX = "delta-"
private const val DEFAULT_DELTA_EXTENSION = "png"

private lateinit var underTest: SnapshotReportFileFinder

internal class SnapshotReportFileFinderTest : FunSpec({

    beforeEach {
        underTest = SnapshotReportFileFinder
    }

    test(
        """
            find files in $DEFAULT_DELTAS_FOLDER folder with $DEFAULT_DELTA_PREFIX prefix 
            and $DEFAULT_DELTA_EXTENSION extension when using default config
        """
    ) {
        // Arrange
        val deltasDir = createSnapshotDeltasDir(deltasDirPath = DEFAULT_DELTAS_FOLDER)
        File(deltasDir, "${DEFAULT_DELTA_PREFIX}a.html").also { it.createNewFile() }
        File(deltasDir, "delt-b.$DEFAULT_DELTA_EXTENSION").also { it.createNewFile() }
        val expectedFiles = listOf(
            File(deltasDir, "${DEFAULT_DELTA_PREFIX}a.$DEFAULT_DELTA_EXTENSION").also { it.createNewFile() },
            File(deltasDir, "${DEFAULT_DELTA_PREFIX}b.$DEFAULT_DELTA_EXTENSION").also { it.createNewFile() },
        )

        // Act
        val actualFiles = underTest.findFiles(
            rootDirectoryPath = rootTempTestDir.toPath(),
            config = SnapshotConfig.DeltaDiscovery(),
        )

        // Assert
        actualFiles.shouldContainExactlyInAnyOrder(expectedFiles)
    }

    test("find files in custom deltas folder") {
        // Arrange
        val customDeltasDirPath = "custom-deltas"
        val deltasDir = createSnapshotDeltasDir(deltasDirPath = customDeltasDirPath)
        val expectedFiles = listOf(
            File(deltasDir, "${DEFAULT_DELTA_PREFIX}a.$DEFAULT_DELTA_EXTENSION").also { it.createNewFile() },
            File(deltasDir, "${DEFAULT_DELTA_PREFIX}b.$DEFAULT_DELTA_EXTENSION").also { it.createNewFile() },
        )

        // Act
        val actualFiles = underTest.findFiles(
            rootDirectoryPath = rootTempTestDir.toPath(),
            config = SnapshotConfig.DeltaDiscovery(deltasFolderPath = customDeltasDirPath),
        )

        // Assert
        actualFiles.shouldContainExactlyInAnyOrder(expectedFiles)
    }

    test("find files with custom prefix and custom extension when using custom config") {
        // Arrange
        val deltasDir = createSnapshotDeltasDir()
        val customPrefix = "CUSTOM"
        val customExtension = "custom"
        File(deltasDir, "delta-a.png").also { it.createNewFile() }
        val expectedFiles = listOf(
            File(deltasDir, "$customPrefix-a.$customExtension").also { it.createNewFile() },
            File(deltasDir, "$customPrefix-b.$customExtension").also { it.createNewFile() },
        )

        // Act
        val actualFiles = underTest.findFiles(
            rootDirectoryPath = rootTempTestDir.toPath(),
            config = SnapshotConfig.DeltaDiscovery(
                deltaFileNamePrefix = customPrefix,
                deltaFileExtension = customExtension,
            ),
        )

        // Assert
        actualFiles.shouldContainExactlyInAnyOrder(expectedFiles)
    }

    test("search only inside build folders and ignore others") {
        // Arrange
        val deltasDir = createSnapshotDeltasDir(buildDirName = "build")
        val expectedFiles =
            listOf(File(deltasDir, "${DEFAULT_DELTA_PREFIX}a.$DEFAULT_DELTA_EXTENSION").also { it.createNewFile() })

        val otherDir = createSnapshotDeltasDir(buildDirName = "other")
        File(otherDir, "${DEFAULT_DELTA_PREFIX}b.$DEFAULT_DELTA_EXTENSION").also { it.createNewFile() }

        // Act
        val actualFiles = underTest.findFiles(
            rootDirectoryPath = rootTempTestDir.toPath(),
            config = SnapshotConfig.DeltaDiscovery(),
        )

        // Assert
        actualFiles shouldHaveSize expectedFiles.size
        actualFiles.shouldContainExactlyInAnyOrder(expectedFiles)
    }

    test("search in all build folders by default") {
        val expectedFiles = listOf("module1", "module2", "module3").map { moduleDirName ->
            val parent = createSnapshotDeltasDir(moduleDirName = moduleDirName)
            File(parent, "${DEFAULT_DELTA_PREFIX}$moduleDirName.$DEFAULT_DELTA_EXTENSION").also { it.createNewFile() }
        }

        val actualFiles = underTest.findFiles(
            rootDirectoryPath = rootTempTestDir.toPath(),
            config = SnapshotConfig.DeltaDiscovery(),
        )

        actualFiles shouldHaveSize expectedFiles.size
        actualFiles.shouldContainExactlyInAnyOrder(expectedFiles)
    }

    test("search in specific build folders if configured") {
        val expectedModules = listOf("module1", "module2")
        val expectedBuildDirs = expectedModules.map { "$it/build" }
        val expectedFiles = expectedModules.map { moduleDirName ->
            val parent = createSnapshotDeltasDir(moduleDirName = moduleDirName)
            File(parent, "${DEFAULT_DELTA_PREFIX}$moduleDirName.$DEFAULT_DELTA_EXTENSION").also { it.createNewFile() }
        }
        File(
            createSnapshotDeltasDir(moduleDirName = "module3"),
            "${DEFAULT_DELTA_PREFIX}module3.$DEFAULT_DELTA_EXTENSION",
        ).also { it.createNewFile() }

        val actualFiles = underTest.findFiles(
            rootDirectoryPath = rootTempTestDir.toPath(),
            config = SnapshotConfig.DeltaDiscovery(
                buildFoldersMatcher = BuildFoldersMatcher.Specific(*expectedBuildDirs.toTypedArray()),
            ),
        )

        actualFiles shouldHaveSize expectedFiles.size
        actualFiles.shouldContainExactlyInAnyOrder(expectedFiles)
    }

    test("find files even in subdirectories of $DEFAULT_DELTAS_FOLDER folder") {
        // Arrange
        val deltasDir = createSnapshotDeltasDir(deltasDirPath = DEFAULT_DELTAS_FOLDER)
        val firstSubFolder = File(deltasDir, "sub1-1/sub1-2/sub1-3").also { it.mkdirs() }
        val secondSubFolder = File(deltasDir, "sub2-1/sub2-2/sub2-3").also { it.mkdirs() }
        val expectedFiles = listOf(
            File(firstSubFolder, "${DEFAULT_DELTA_PREFIX}a.$DEFAULT_DELTA_EXTENSION").also { it.createNewFile() },
            File(secondSubFolder, "${DEFAULT_DELTA_PREFIX}b.$DEFAULT_DELTA_EXTENSION").also { it.createNewFile() },
        )

        // Act
        val actualFiles = underTest.findFiles(
            rootDirectoryPath = rootTempTestDir.toPath(),
            config = SnapshotConfig.DeltaDiscovery(),
        )

        // Assert
        actualFiles.shouldContainExactlyInAnyOrder(expectedFiles)
    }

    test("do not fail when no files found") {
        shouldNotThrowAny {
            underTest.findFiles(
                rootDirectoryPath = createSnapshotDeltasDir().toPath(),
                config = SnapshotConfig.DeltaDiscovery(),
            )
        }
    }

    test("do not fail when folder without parent is passed as root directory for search") {
        shouldNotThrow<NullPointerException> {
            try {
                underTest.findFiles(
                    rootDirectoryPath = Paths.get(""),
                    config = SnapshotConfig.DeltaDiscovery(),
                )
            } catch (_: NoFilesFoundException) {
                // Might be thrown because we check real build folder here and there might be no test results
            }
        }
    }
})

private fun TestConfiguration.createSnapshotDeltasDir(
    moduleDirName: String = "app",
    buildDirName: String = "build",
    deltasDirPath: String = DEFAULT_DELTAS_FOLDER,
): File {
    val moduleDir = File(rootTempTestDir, moduleDirName)
    val buildDir = File(moduleDir, buildDirName)
    return File(buildDir, deltasDirPath).also { it.mkdirs() }
}
