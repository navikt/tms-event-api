package no.nav.tms.event.api.config

import io.ktor.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import no.nav.tms.event.api.beskjed.BeskjedConsumer
import no.nav.tms.event.api.beskjed.BeskjedEventService
import no.nav.tms.event.api.common.AzureTokenFetcher
import no.nav.tms.event.api.health.HealthService
import no.nav.tms.event.api.innboks.InnboksConsumer
import no.nav.tms.event.api.innboks.InnboksEventService
import no.nav.tms.event.api.oppgave.OppgaveConsumer
import no.nav.tms.event.api.oppgave.OppgaveReader
import no.nav.tms.token.support.azure.exchange.AzureServiceBuilder
import no.nav.tms.token.support.azure.validation.installAzureAuth
import java.net.URL

fun main() {
    val environment = Environment()

    val httpClient = HttpClientBuilder.build()

    val azureService = AzureServiceBuilder.buildAzureService(enableDefaultProxy = true)
    val azureTokenFetcher = AzureTokenFetcher(azureService, environment.eventHandlerClientId)

    val beskjedConsumer = BeskjedConsumer(httpClient, URL(environment.eventHandlerUrl))
    val beskjedEventService = BeskjedEventService(beskjedConsumer, azureTokenFetcher)

    val oppgaveConsumer = OppgaveConsumer(httpClient, URL(environment.eventHandlerUrl))
    val oppgaveReader = OppgaveReader(oppgaveConsumer, azureTokenFetcher)

    val innboksConsumer = InnboksConsumer(httpClient, URL(environment.eventHandlerUrl))
    val innboksEventService = InnboksEventService(innboksConsumer, azureTokenFetcher)

    embeddedServer(Netty, port = 8080) {
        api(
            healthService = HealthService(),
            beskjedEventService = beskjedEventService,
            oppgaveReader = oppgaveReader,
            innboksEventService = innboksEventService,
            authConfig = authConfigBuilder(),
            httpClient = httpClient
        )
    }.start(wait = true)
}

private fun authConfigBuilder(): Application.() -> Unit = {
    installAzureAuth {
        setAsDefault = true
    }
}
