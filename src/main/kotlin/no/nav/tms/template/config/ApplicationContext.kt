package no.nav.tms.template.config

import no.nav.tms.template.health.HealthService

class ApplicationContext {

    val httpClient = HttpClientBuilder.build()
    val healthService = HealthService(this)

}
