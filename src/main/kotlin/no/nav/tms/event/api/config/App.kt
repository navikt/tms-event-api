package no.nav.tms.event.api.config

import io.ktor.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import no.nav.tms.event.api.beskjed.BeskjedVarselReader
import no.nav.tms.event.api.common.AzureTokenFetcher
import no.nav.tms.event.api.health.HealthService
import no.nav.tms.event.api.innboks.InnboksVarselReader
import no.nav.tms.event.api.oppgave.OppgaveVarselReader
import no.nav.tms.token.support.azure.exchange.AzureServiceBuilder
import no.nav.tms.token.support.azure.validation.installAzureAuth

fun main() {
    val environment = Environment()

    val httpClient = HttpClientBuilder.build()

    val azureService = AzureServiceBuilder.buildAzureService(enableDefaultProxy = true)
    val azureTokenFetcher = AzureTokenFetcher(azureService, environment.eventHandlerClientId)

    val beskjedVarselReader = BeskjedVarselReader(
        azureTokenFetcher = azureTokenFetcher,
        client = httpClient,
        eventHandlerBaseURL = environment.eventHandlerUrl
    )

    val oppgaveVarselReader = OppgaveVarselReader(
        azureTokenFetcher = azureTokenFetcher,
        client = httpClient,
        eventHandlerBaseURL = environment.eventHandlerUrl
    )

    val innboksVarselReader = InnboksVarselReader(
        azureTokenFetcher = azureTokenFetcher,
        eventHandlerBaseURL = environment.eventHandlerUrl,
        client = httpClient
    )

    embeddedServer(Netty, port = 8080) {
        api(
            healthService = HealthService(),
            beskjedVarselReader = beskjedVarselReader,
            oppgaveVarselReader = oppgaveVarselReader,
            innboksVarselReader = innboksVarselReader,
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
