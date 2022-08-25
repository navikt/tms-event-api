package no.nav.tms.event.api.health

class HealthService {
    suspend fun getHealthChecks(): List<HealthStatus> {
        return emptyList()
    }
}
