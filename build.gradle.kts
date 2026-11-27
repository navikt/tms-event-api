import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    alias(managed.plugins.kotlin)
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
    implementation(managed.ktor.server.swagger)
    implementation(managed.tms.ktorTokenSupport.entraId.fetcher)
    implementation(managed.tms.ktorTokenSupport.entraId.verification)
    implementation(managed.tms.commonLib.metrics)
    implementation(managed.tms.commonLib.observability)
    implementation(managed.tms.commonLib.teamLogger)
    implementation(managed.tms.commonLib.utils)


    runtimeOnly(managed.logback.encoder)

    testImplementation(managed.junit.api)
    testImplementation(managed.junit.params)

    testImplementation(managed.ktor.test.server)
    testImplementation(managed.tms.ktorTokenSupport.entraId.verificationMock)
    testImplementation(managed.kotest.assertion)
    testImplementation(managed.mockk)

    testRuntimeOnly(managed.junit.engine)
    testRuntimeOnly(managed.junit.launcher)
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
