import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    kotlin("jvm").version(managed.versions.kotlin)

    alias(managed.plugins.tms.jarBundling)

    application
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }
    mavenLocal()
}

dependencies {
    implementation(managed.bundles.jackson)
    implementation(managed.bundles.ktorClient)
    implementation(managed.bundles.ktorServer)

    implementation(managed.kotlinLogging)
    implementation(managed.ktor.serialization.jackson)
    implementation(managed.ktor.server.defaultHeaders)
    implementation(managed.logback.encoder)
    implementation(managed.tms.ktorTokenSupport.entraId.fetcher)
    implementation(managed.tms.ktorTokenSupport.entraId.verification)
    implementation(managed.tms.commonLib.metrics)
    implementation(managed.tms.commonLib.observability)
    implementation(managed.tms.commonLib.teamLogger)
    implementation(managed.tms.commonLib.utils)


    testImplementation(managed.bundles.junit)

    testImplementation(managed.junit.params)

    testImplementation(managed.ktor.test.clientMock)
    testImplementation(managed.ktor.test.server)
    testImplementation(managed.tms.ktorTokenSupport.entraId.verificationMock)
    testImplementation(managed.kotest.assertion)
    testImplementation(managed.mockk)
}

application {
    mainClass.set("no.nav.tms.event.api.AppKt")
}

tasks {
    withType<Test> {
        useJUnitPlatform()
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            events("passed", "skipped", "failed")
        }
    }
}
