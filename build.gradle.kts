import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin on the JVM.
    kotlin("jvm").version(Kotlin.version)
    kotlin("plugin.serialization").version(Kotlin.version)

    id(Shadow.pluginId) version (Shadow.version)
    // Apply the application plugin to add support for building a CLI application.
    application
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
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
    implementation(KotlinLogging.logging)
    implementation(Ktor.Server.core)
    implementation(Ktor.Server.netty)
    implementation(Ktor.Server.metricsMicrometer)
    implementation(Ktor.Server.defaultHeaders)
    implementation(Ktor.Server.auth)
    implementation(Ktor.Server.contentNegotiation)
    implementation(Ktor.Server.statusPages)
    implementation(Ktor.Client.core)
    implementation(Ktor.Client.apache)
    implementation(Ktor.Client.contentNegotiation)
    implementation(Ktor.Serialization.kotlinX)
    implementation(Logstash.logbackEncoder)
    implementation(TmsKtorTokenSupport.azureExchange)
    implementation(TmsKtorTokenSupport.azureValidation)
    implementation(TmsCommonLib.utils)
    implementation(TmsCommonLib.metrics)
    implementation(TmsCommonLib.observability)

    testImplementation(Junit.api)
    testImplementation(Ktor.Serialization.jackson)
    testImplementation(Ktor.Test.clientMock)
    testImplementation(Ktor.Test.serverTestHost)
    testImplementation(TmsKtorTokenSupport.azureValidationMock)
    testImplementation(Kluent.kluent)
    testImplementation(Mockk.mockk)
    testImplementation(Junit.params)
    testImplementation("io.ktor:ktor-server-test-host-jvm:2.3.5")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.9.10")
    testImplementation(TmsTestUtils.testUtils)

    testRuntimeOnly(Jjwt.impl)
    testRuntimeOnly(Junit.engine)
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
