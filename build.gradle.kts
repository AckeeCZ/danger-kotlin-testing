plugins {
    alias(libs.plugins.detekt)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.mavenPublish)

    alias(libs.plugins.ackeecz.danger.testing.detekt)
    alias(libs.plugins.ackeecz.danger.testing.publishing)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)

    compilerOptions {
        freeCompilerArgs.add("-Xexplicit-api=strict")
    }
}

dependencies {

    implementation(libs.danger.kotlin.sdk)
    implementation(libs.jackson.xml)
    implementation(libs.jackson.kotlinModule)

    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.runner.junit5)
}
