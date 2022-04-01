package no.nav.tms.event.api.config

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.features.*
import io.ktor.routing.*
import io.ktor.serialization.*
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
