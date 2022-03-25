package no.nav.tms.template.health

interface HealthCheck {

    suspend fun status(): HealthStatus

}
