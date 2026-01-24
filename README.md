[ ![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.ackeecz/danger-kotlin-testing/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.ackeecz/danger-kotlin-testing)

# danger-kotlin testing plugin

Plugin for [danger-kotlin](https://github.com/danger/kotlin) that processes test results.

Although this plugin is open for general use, it’s primarily designed as an internal tool to help us share Danger 
testing logic across our Ackee projects. Because of that, some defaults and configuration choices are intentionally 
opinionated to better support our internal workflows.

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
TestingPlugin.findAndProcessJUnitReports(JUnitConfig)
TestingPlugin.findAndProcessSnapshotReports(SnapshotConfig)
```

`findAndProcessJUnitReports` method finds and parses XML outputs of JUnit tests and reports failures to the pull request.
It accepts an optional configuration object that allows to customize some behavior.

`findAndProcessSnapshotReports` method finds and processes failed snapshot delta image files and reports them to the pull request.
It accepts an optional configuration object that allows to customize some behavior.
It is necessary to call this method only from the GitLab CI pipeline.

Example Dangerfile

```kotlin
@file:DependsOn("io.github.ackeecz:danger-kotlin-testing:x.y.z")

import io.github.ackeecz.danger.testing.TestingPlugin
import io.github.ackeecz.danger.testing.junit.JUnitConfig
import io.github.ackeecz.danger.testing.snapshot.SnapshotConfig        

import systems.danger.kotlin.danger
import systems.danger.kotlin.register

import java.nio.file.Files
import java.nio.file.Paths
import java.util.function.BiPredicate
import java.util.stream.Collectors

register plugin TestingPlugin

danger(args) {
    TestingPlugin.findAndProcessJUnitReports(
        // Optional config
        JUnitConfig()
    )
    TestingPlugin.findAndProcessSnapshotReports(
        // Optional config
        SnapshotConfig()
    )
}
```
