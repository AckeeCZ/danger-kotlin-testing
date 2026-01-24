[ ![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.ackeecz/danger-kotlin-testing/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.ackeecz/danger-kotlin-testing)

# danger-kotlin testing plugin

Plugin for [danger-kotlin](https://github.com/danger/kotlin) that processes test results.

## Installation

Put

```kotlin
@file:DependsOn("io.github.ackeecz:danger-kotlin-testing:x.y.z")
```

to the top of your Dangerfile

## Usage

First you need to register the plugin via

```kotlin
register plugin TestingPlugin
```

and then you can use it through its public methods

```kotlin
TestingPlugin.parse(junitReportFile)
TestingPlugin.report()
```

`parse` method accepts varargs of files pointing to the junit reports and parses them to internal representation.

`report` methods will process parsed results and reports them to pull request comments.

Example Dangerfile

```kotlin
@file:DependsOn("io.github.ackeecz:danger-kotlin-testing:x.y.z")

import io.github.ackeecz.danger.testing.TestingPlugin

import systems.danger.kotlin.danger
import systems.danger.kotlin.register

import java.nio.file.Files
import java.nio.file.Paths
import java.util.function.BiPredicate
import java.util.stream.Collectors

register plugin TestingPlugin

danger(args) {
    val junitReports = Files.find(Paths.get(""), 10, BiPredicate { path, _ ->
        val fileName = path.toFile().name
        fileName.startsWith("TEST") && fileName.endsWith("xml")
    }).map { it.toFile() }.collect(Collectors.toList())

    TestingPlugin.parse(*junitFiles.toTypedArray())
    TestingPlugin.report()
}
```

This will find all files in the depth of 10 relative to current directory that matches the junit report files naming,
and it will pass them to the plugin for processing.
