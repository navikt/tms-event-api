package no.nav.tms.template.health

import no.nav.tms.template.config.ApplicationContext

class HealthService(private val applicationContext: ApplicationContext) {

    suspend fun getHealthChecks(): List<HealthStatus> {
        return emptyList()
    }
}
