package no.nav.tms.event.api.config

import no.nav.tms.event.api.health.HealthService

class ApplicationContext {

    val httpClient = HttpClientBuilder.build()
    val healthService = HealthService(this)

}
