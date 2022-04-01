package no.nav.tms.event.api.health

interface HealthCheck {

    suspend fun status(): HealthStatus

}
