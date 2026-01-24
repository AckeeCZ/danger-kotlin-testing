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
TestingPlugin.findAndProcessJUnitReports(JUnitConfig)
```

`findAndProcessJUnitReports` method finds and parses XML outputs of JUnit tests and reports failures to the pull request.
It accepts an optional configuration object that allows to customize some behavior.

Example Dangerfile

```kotlin
@file:DependsOn("io.github.ackeecz:danger-kotlin-testing:x.y.z")

import io.github.ackeecz.danger.testing.TestingPlugin
import io.github.ackeecz.danger.testing.junit.JUnitConfig

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
}
```
