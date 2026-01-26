package io.github.ackeecz.danger.testing.junit

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import systems.danger.kotlin.sdk.DangerContext
import tools.jackson.dataformat.xml.XmlMapper
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty
import java.io.File
import java.io.FileInputStream

internal class JUnitReportProcessor(private val dangerContext: DangerContext) {

    fun process(reportFiles: List<File>) {
        val failingTestSuites = parseFailingTestSuites(reportFiles)
        report(failingTestSuites)
    }

    private fun parseFailingTestSuites(reportFiles: List<File>): List<FailingTestSuite> {
        val mapper = XmlMapper()
        return reportFiles.mapNotNull { file ->
            FileInputStream(file).use { fileInputStream ->
                val testSuite = parseJUnitReport(mapper, fileInputStream)
                val failingTestCases = testSuite.testCases
                    .filter { it.failures.isNotEmpty() }
                    .map { testCase ->
                        FailingTestCase(
                            name = testCase.name,
                            failureMessages = testCase.failures.mapNotNull { it.message?.substringAfter(": ") },
                        )
                    }
                if (failingTestCases.isNotEmpty()) {
                    FailingTestSuite(testSuite.name, failingTestCases)
                } else {
                    null
                }
            }
        }
    }

    private fun parseJUnitReport(
        mapper: XmlMapper,
        fileInputStream: FileInputStream
    ): TestSuite {
        return mapper.readValue(
            fileInputStream,
            TestSuite::class.java
        )
    }

    private fun report(failingTestSuites: List<FailingTestSuite>) {
        failingTestSuites.takeIf { it.isNotEmpty() }?.let { failingTestSuites ->
            val message = buildString {
                append("## ❌ Failed tests \n\n")
                failingTestSuites.forEach { testSuite ->
                    append("### `${testSuite.className}`\n\n")
                    testSuite.failingTestCases.forEach { testCase ->
                        append("- **${testCase.name}**\n")
                        testCase.failureMessages.forEach { failureMessage ->
                            append("  - $failureMessage\n")
                        }
                    }
                    append("\n")
                }
            }
            dangerContext.markdown(message)
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonRootName(value = "testsuite")
private data class TestSuite(
    @field:JacksonXmlProperty val name: String = "",
    @field:JsonProperty("testcase")
    @field:JacksonXmlElementWrapper(useWrapping = false) val testCases: List<TestCase> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class TestCase(
    @field:JacksonXmlProperty val name: String = "",
    @field:JsonProperty("failure")
    @field:JacksonXmlElementWrapper(useWrapping = false) val failures: List<Failure> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class Failure(
    @field:JacksonXmlProperty(isAttribute = true) val message: String? = null,
)

private data class FailingTestSuite(
    val className: String,
    val failingTestCases: List<FailingTestCase>
)

private data class FailingTestCase(
    val name: String,
    val failureMessages: List<String>,
)
