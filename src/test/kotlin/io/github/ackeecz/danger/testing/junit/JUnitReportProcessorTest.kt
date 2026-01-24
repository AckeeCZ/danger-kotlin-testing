package io.github.ackeecz.danger.testing.junit

import io.github.ackeecz.danger.testing.FakeDangerContext
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import java.io.File

private lateinit var dangerContext: FakeDangerContext
private lateinit var underTest: JUnitReportProcessor

internal class JUnitReportProcessorTest : FunSpec({

    beforeEach {
        dangerContext = FakeDangerContext()
        underTest = JUnitReportProcessor(dangerContext)
    }

    test("should report failures") {
        val files = listOf(File(ClassLoader.getSystemResource("test_failures.xml").toURI()))

        underTest.process(files)

        dangerContext.markdowns.shouldNotBeEmpty()
    }

    test("should not report success") {
        val files = listOf(File(ClassLoader.getSystemResource("test_success.xml").toURI()))

        underTest.process(files)

        dangerContext.markdowns.shouldBeEmpty()
    }
})
