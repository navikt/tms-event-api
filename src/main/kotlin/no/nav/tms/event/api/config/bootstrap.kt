package no.nav.tms.event.api.config

import io.ktor.application.Application
import io.ktor.application.ApplicationStopping
import io.ktor.application.install
import io.ktor.auth.authenticate
import io.ktor.client.HttpClient
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.routing.routing
import io.ktor.serialization.json
import no.nav.tms.event.api.beskjed.beskjedApi
import no.nav.tms.event.api.health.healthApi
import no.nav.tms.event.api.innboks.innboksApi
import no.nav.tms.event.api.oppgave.oppgaveApi
import no.nav.tms.token.support.azure.validation.installAzureAuth

fun Application.mainModule(appContext: ApplicationContext = ApplicationContext()) {

    install(DefaultHeaders)

    installAzureAuth {
        setAsDefault = true
    }

    install(ContentNegotiation) {
        json(jsonConfig())
    }

    routing {
        healthApi(appContext.healthService)
        authenticate {
            oppgaveApi(appContext.oppgaveEventService)
            beskjedApi(appContext.beskjedEventService)
            innboksApi(appContext.innboksEventService)
        }
    }

    configureShutdownHook(appContext.httpClient)
}

private fun Application.configureShutdownHook(httpClient: HttpClient) {
    environment.monitor.subscribe(ApplicationStopping) {
        httpClient.close()
    }
}
