package io.github.ackeecz.danger.testing.junit

import io.github.ackeecz.danger.testing.BuildFoldersMatcher
import io.github.ackeecz.danger.testing.FakeDangerContext
import io.github.ackeecz.danger.testing.NoFilesFoundException
import io.github.ackeecz.danger.testing.util.rootTempTestDir
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.TestConfiguration
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Paths

private const val DEFAULT_TEST_RESULTS_FOLDER = "test-results"
private const val DEFAULT_FILE_PREFIX = "TEST-"

private lateinit var dangerContext: FakeDangerContext
private lateinit var underTest: JUnitResultFileFinder

internal class JUnitReportFileFinderTest : FunSpec({

    beforeEach {
        dangerContext = FakeDangerContext()
        underTest = JUnitResultFileFinder(dangerContext)
    }

    test("find .xml files with $DEFAULT_FILE_PREFIX prefix in $DEFAULT_TEST_RESULTS_FOLDER folder when using default config") {
        // Arrange
        val resultsDir = createTestResultsDir(testResultsDirPath = DEFAULT_TEST_RESULTS_FOLDER)
        File(resultsDir, "${DEFAULT_FILE_PREFIX}a.html").also { it.createNewFile() }
        File(resultsDir, "TES-a.xml").also { it.createNewFile() }
        val expectedFiles = listOf(
            File(resultsDir, "${DEFAULT_FILE_PREFIX}a.xml").also { it.createNewFile() },
            File(resultsDir, "${DEFAULT_FILE_PREFIX}b.xml").also { it.createNewFile() },
        )

        // Act
        val actualFiles = underTest.findFiles(
            rootDirectoryPath = rootTempTestDir.toPath(),
            config = JUnitConfig.Discovery(),
        )

        // Assert
        actualFiles.shouldContainExactlyInAnyOrder(expectedFiles)
    }

    test("find *.xml files with custom prefix when using custom config") {
        // Arrange
        val resultsDir = createTestResultsDir()
        val customPrefix = "CUSTOM"
        File(resultsDir, "${DEFAULT_FILE_PREFIX}a.xml").also { it.createNewFile() }
        val expectedFiles = listOf(
            File(resultsDir, "$customPrefix-a.xml").also { it.createNewFile() },
            File(resultsDir, "$customPrefix-b.xml").also { it.createNewFile() },
        )

        // Act
        val actualFiles = underTest.findFiles(
            rootDirectoryPath = rootTempTestDir.toPath(),
            config = JUnitConfig.Discovery(resultFileNamePrefix = customPrefix),
        )

        // Assert
        actualFiles.shouldContainExactlyInAnyOrder(expectedFiles)
    }

    test("find *.xml files in custom test results folder") {
        // Arrange
        val customTestResultsFolderPath = "custom-test-results"
        val resultsDir = createTestResultsDir(testResultsDirPath = customTestResultsFolderPath)
        val expectedFiles = listOf(
            File(resultsDir, "${DEFAULT_FILE_PREFIX}a.xml").also { it.createNewFile() },
            File(resultsDir, "${DEFAULT_FILE_PREFIX}b.xml").also { it.createNewFile() },
        )

        // Act
        val actualFiles = underTest.findFiles(
            rootDirectoryPath = rootTempTestDir.toPath(),
            config = JUnitConfig.Discovery(testResultsFolderPath = customTestResultsFolderPath),
        )

        // Assert
        actualFiles.shouldContainExactlyInAnyOrder(expectedFiles)
    }

    test("search only inside build folders and ignore others") {
        // Arrange
        val resultsDir = createTestResultsDir(buildDirName = "build")
        val expectedFiles = listOf(File(resultsDir, "${DEFAULT_FILE_PREFIX}a.xml").also { it.createNewFile() })

        val otherDir = createTestResultsDir(buildDirName = "other")
        File(otherDir, "${DEFAULT_FILE_PREFIX}b.xml").also { it.createNewFile() }

        // Act
        val actualFiles = underTest.findFiles(
            rootDirectoryPath = rootTempTestDir.toPath(),
            config = JUnitConfig.Discovery(),
        )

        // Assert
        actualFiles shouldHaveSize expectedFiles.size
        actualFiles.shouldContainExactlyInAnyOrder(expectedFiles)
    }

    test("search in all build folders by default") {
        val expectedFiles = listOf("module1", "module2", "module3").map { moduleDirName ->
            val parent = createTestResultsDir(moduleDirName = moduleDirName)
            File(parent, "${DEFAULT_FILE_PREFIX}$moduleDirName.xml").also { it.createNewFile() }
        }

        val actualFiles = underTest.findFiles(
            rootDirectoryPath = rootTempTestDir.toPath(),
            config = JUnitConfig.Discovery(),
        )

        actualFiles shouldHaveSize expectedFiles.size
        actualFiles.shouldContainExactlyInAnyOrder(expectedFiles)
    }

    test("search in specific build folders if configured") {
        val expectedModules = listOf("module1", "module2")
        val expectedBuildDirs = expectedModules.map { "$it/build" }
        val expectedFiles = expectedModules.map { moduleDirName ->
            val parent = createTestResultsDir(moduleDirName = moduleDirName)
            File(parent, "${DEFAULT_FILE_PREFIX}$moduleDirName.xml").also { it.createNewFile() }
        }
        File(createTestResultsDir(moduleDirName = "module3"), "module3.xml").also { it.createNewFile() }

        val actualFiles = underTest.findFiles(
            rootDirectoryPath = rootTempTestDir.toPath(),
            config = JUnitConfig.Discovery(
                buildFoldersMatcher = BuildFoldersMatcher.Specific(*expectedBuildDirs.toTypedArray()),
            ),
        )

        actualFiles shouldHaveSize expectedFiles.size
        actualFiles.shouldContainExactlyInAnyOrder(expectedFiles)
    }

    test("find files even in subdirectories of $DEFAULT_TEST_RESULTS_FOLDER folder") {
        // Arrange
        val resultsDir = createTestResultsDir(testResultsDirPath = DEFAULT_TEST_RESULTS_FOLDER)
        val firstSubFolder = File(resultsDir, "sub1-1/sub1-2/sub1-3").also { it.mkdirs() }
        val secondSubFolder = File(resultsDir, "sub2-1/sub2-2/sub2-3").also { it.mkdirs() }
        val expectedFiles = listOf(
            File(firstSubFolder, "${DEFAULT_FILE_PREFIX}a.xml").also { it.createNewFile() },
            File(secondSubFolder, "${DEFAULT_FILE_PREFIX}b.xml").also { it.createNewFile() },
        )

        // Act
        val actualFiles = underTest.findFiles(
            rootDirectoryPath = rootTempTestDir.toPath(),
            config = JUnitConfig.Discovery(),
        )

        // Assert
        actualFiles.shouldContainExactlyInAnyOrder(expectedFiles)
    }

    test("warn when no files found") {
        underTest.findFiles(
            rootDirectoryPath = createTestResultsDir().toPath(),
            config = JUnitConfig.Discovery(),
        )

        dangerContext.warnings shouldHaveSize 1
        dangerContext.warnings.first().message shouldBe "No JUnit test results found"
    }

    test("do not fail when folder without parent is passed as root directory for search") {
        shouldNotThrow<NullPointerException> {
            try {
                underTest.findFiles(
                    rootDirectoryPath = Paths.get(""),
                    config = JUnitConfig.Discovery(),
                )
            } catch (_: NoFilesFoundException) {
                // Might be thrown because we check real build folder here and there might be no test results
            }
        }
    }
})

private fun TestConfiguration.createTestResultsDir(
    moduleDirName: String = "app",
    buildDirName: String = "build",
    testResultsDirPath: String = DEFAULT_TEST_RESULTS_FOLDER,
): File {
    val moduleDir = File(rootTempTestDir, moduleDirName)
    val buildDir = File(moduleDir, buildDirName)
    return File(buildDir, testResultsDirPath).also { it.mkdirs() }
}
