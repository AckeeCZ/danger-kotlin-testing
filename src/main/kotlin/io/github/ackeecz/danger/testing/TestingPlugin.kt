package io.github.ackeecz.danger.testing

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import systems.danger.kotlin.sdk.DangerPlugin
import tools.jackson.dataformat.xml.XmlMapper
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty
import tools.jackson.dataformat.xml.annotation.JacksonXmlText
import java.io.File
import java.io.FileInputStream

/**
 * Danger-kotlin plugin for processing test reports and print failures
 * to the PR comment.
 */
public object TestingPlugin : DangerPlugin() {

    override val id: String = "danger-kotlin-testing"

    private var failingTestSuites: List<FailingTestSuite>? = null

    /**
     * Process [files] representing junit results
     */
    public fun parse(vararg files: File) {
        val mapper = XmlMapper()
        failingTestSuites = files.mapNotNull { file ->
            FileInputStream(file).use { fileInputStream ->
                val testSuite = parse(mapper, fileInputStream)
                val failingTestCases = testSuite.testCases
                    .filter { testCase ->
                        testCase.failures.isNotEmpty()
                    }
                    .map { testCase ->
                        FailingTestCase(testCase.name)
                    }
                if (failingTestCases.isNotEmpty()) {
                    FailingTestSuite(testSuite.name, failingTestCases)
                } else {
                    null
                }
            }
        }
    }

    private fun parse(
        mapper: XmlMapper,
        fileInputStream: FileInputStream
    ): TestSuite {
        return mapper.readValue(
            fileInputStream,
            TestSuite::class.java
        )
    }

    /**
     * Report parsed junit results to the PR
     */
    public fun report() {
        failingTestSuites?.takeIf { it.isNotEmpty() }?.let { failingTestSuites ->
            context.fail("Tests have failed. See below for more information")
            val message = buildString {
                append("### Tests: \n\n")

                failingTestSuites.forEach { testSuite ->
                    append("#### ${testSuite.className}\n\n")
                    append("| File | Name |\n")
                    append("| ---- | ---- |\n")
                    testSuite.failingTestCases.forEach { testCase ->
                        append("| ${testSuite.className} | ${testCase.name} |\n")
                    }
                    append("\n\n")
                }
            }
            context.markdown(message)
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonRootName(value = "testsuite")
internal data class TestSuite(
    @field:JacksonXmlProperty val name: String = "",
    @field:JsonProperty("testcase")
    @field:JacksonXmlElementWrapper(useWrapping = false) val testCases: List<TestCase> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class TestCase(
    @field:JacksonXmlProperty val name: String = "",
    @field:JsonProperty("failure")
    @field:JacksonXmlElementWrapper(useWrapping = false) val failures: List<Failure> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class Failure(
    @field:JacksonXmlProperty val message: String = "",
    @field:JacksonXmlProperty val type: String = "",
    @field:JacksonXmlText val body: String = ""
)

internal data class FailingTestSuite(
    val className: String,
    val failingTestCases: List<FailingTestCase>
)

internal data class FailingTestCase(val name: String)
