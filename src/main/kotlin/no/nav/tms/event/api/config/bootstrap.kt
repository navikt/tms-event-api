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
import no.nav.tms.event.api.api.healthApi
import no.nav.tms.event.api.api.innboksApi
import no.nav.tms.event.api.api.oppgaveApi
import no.nav.tms.event.api.beskjed.BeskjedVarselReader
import no.nav.tms.event.api.beskjed.beskjedApi
import no.nav.tms.event.api.innboks.InnboksVarselReader
import no.nav.tms.event.api.oppgave.OppgaveVarselReader

fun Application.api(
    beskjedVarselReader: BeskjedVarselReader,
    oppgaveVarselReader: OppgaveVarselReader,
    innboksVarselReader: InnboksVarselReader,
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
            healthApi()
            authenticate {
                oppgaveApi(oppgaveVarselReader)
                beskjedApi(beskjedVarselReader)
                innboksApi(innboksVarselReader)
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
