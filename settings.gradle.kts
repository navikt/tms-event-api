rootProject.name = "tms-event-api"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven(url = uri("https://github-package-registry-mirror.gc.nav.no/cached/maven-release"))
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }
    versionCatalogs {
        create("managed") {
            from("no.nav.tms:tms-dependency-admin:20261027.8.1")
        }
    }
}
