import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin on the JVM.
    kotlin("jvm").version(Kotlin.version)
    kotlin("plugin.serialization").version(Kotlin.version)
    kotlin("plugin.allopen").version(Kotlin.version)

    id(Shadow.pluginId) version (Shadow.version)
    // Apply the application plugin to add support for building a CLI application.
    application

    // Ktlint
    id("org.jlleitschuh.gradle.ktlint").version("10.3.0")
    id("com.faire.gradle.analyze") version "1.0.9"
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    mavenLocal()
}

dependencies {
    implementation(DittNAVCommonLib.utils)
    implementation(KotlinLogging.logging)
    implementation(Kotlinx.coroutines)
    implementation(Ktor2.Server.core)
    implementation(Ktor2.Server.netty)
    implementation(Ktor2.Server.metricsMicrometer)
    implementation(Ktor2.Server.defaultHeaders)
    implementation(Ktor2.Server.auth)
    implementation(Ktor2.Server.contentNegotiation)
    implementation(Ktor2.Client.core)
    implementation(Ktor2.Client.apache)
    implementation(Ktor2.Client.contentNegotiation)
    implementation(TmsKtorTokenSupport.azureExchange)
    implementation(TmsKtorTokenSupport.azureValidation)
    implementation(Ktor2.Serialization.kotlinX)

    implementation(Micrometer.registryPrometheus)
    implementation(Logback.classic)
    implementation(Logstash.logbackEncoder)

    testImplementation(Junit.api)
    testImplementation(Ktor2.Serialization.jackson)
    testImplementation(Ktor2.Test.clientMock)
    testImplementation(Ktor2.Test.serverTestHost)
    testImplementation(TmsKtorTokenSupport.authenticationInstallerMock)
    testImplementation(TmsKtorTokenSupport.tokenXValidationMock)
    testImplementation(Kluent.kluent)
    testImplementation(Mockk.mockk)
    testImplementation(Junit.params)

    testRuntimeOnly(Bouncycastle.bcprovJdk15on)
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

// TODO: Fjern følgende work around i ny versjon av Shadow-pluginet:
// Skal være løst i denne: https://github.com/johnrengelman/shadow/pull/612
project.setProperty("mainClassName", application.mainClass.get())
apply(plugin = Shadow.pluginId)
