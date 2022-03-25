package no.nav.tms.template.health

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get

fun Routing.healthApi(healthService: HealthService) {

    val pingJsonResponse = """{"ping": "pong"}"""

    get("/internal/ping") {
        call.respondText(pingJsonResponse, ContentType.Application.Json)
    }

    get("/internal/isAlive") {
        call.respondText(text = "ALIVE", contentType = ContentType.Text.Plain)
    }

    get("/internal/isReady") {
        if (isReady(healthService)) {
            call.respondText(text = "READY", contentType = ContentType.Text.Plain)
        } else {
            call.respondText(text = "NOTREADY", contentType = ContentType.Text.Plain, status = HttpStatusCode.ServiceUnavailable)
        }
    }

    get("/internal/selftest") {
        call.buildSelftestPage(healthService)
    }
}

private suspend fun isReady(healthService: HealthService): Boolean {
    val healthChecks = healthService.getHealthChecks()
    return healthChecks
            .filter { healthStatus -> healthStatus.includeInReadiness }
            .all { healthStatus -> Status.OK == healthStatus.status }
}
