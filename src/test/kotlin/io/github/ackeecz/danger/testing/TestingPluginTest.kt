package io.github.ackeecz.danger.testing

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import java.io.File

internal class TestingPluginTest : FunSpec({

    test("should report failures") {
        val dangerContext = FakeDangerContext()
        TestingPlugin.context = dangerContext

        TestingPlugin.parse(File(ClassLoader.getSystemResource("test_failures.xml").toURI()))
        TestingPlugin.report()

        dangerContext.markdowns.shouldNotBeEmpty()
    }

    test("should not report success") {
        val dangerContext = FakeDangerContext()
        TestingPlugin.context = dangerContext

        TestingPlugin.parse(File(ClassLoader.getSystemResource("test_success.xml").toURI()))
        TestingPlugin.report()

        dangerContext.markdowns.shouldBeEmpty()
    }
})
