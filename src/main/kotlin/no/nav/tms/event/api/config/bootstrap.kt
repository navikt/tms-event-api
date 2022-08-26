package no.nav.tms.event.api.config

import io.ktor.application.Application
import io.ktor.application.ApplicationStopping
import io.ktor.application.install
import io.ktor.auth.authenticate
import io.ktor.client.HttpClient
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.serialization.json
import no.nav.tms.event.api.beskjed.BeskjedEventService
import no.nav.tms.event.api.beskjed.beskjedApi
import no.nav.tms.event.api.health.HealthService
import no.nav.tms.event.api.health.healthApi
import no.nav.tms.event.api.innboks.InnboksEventService
import no.nav.tms.event.api.innboks.innboksApi
import no.nav.tms.event.api.oppgave.OppgaveReader
import no.nav.tms.event.api.oppgave.oppgaveApi

fun Application.api(
    healthService: HealthService,
    beskjedEventService: BeskjedEventService,
    oppgaveReader: OppgaveReader,
    innboksEventService: InnboksEventService,
    httpClient: HttpClient,
    authConfig: Application.() -> Unit
) {

    install(DefaultHeaders)

    authConfig()

    install(ContentNegotiation) {
        json(jsonConfig())
    }

    routing {
        route("/tms-event-api") {
            healthApi(healthService)
            authenticate {
                oppgaveApi(oppgaveReader)
                beskjedApi(beskjedEventService)
                innboksApi(innboksEventService)
            }
        }
    }

    configureShutdownHook(httpClient)
}

private fun Application.configureShutdownHook(httpClient: HttpClient) {
    environment.monitor.subscribe(ApplicationStopping) {
        httpClient.close()
    }
}
