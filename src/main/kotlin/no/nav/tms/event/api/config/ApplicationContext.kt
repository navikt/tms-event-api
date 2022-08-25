package no.nav.tms.event.api.config

import no.nav.tms.event.api.beskjed.BeskjedConsumer
import no.nav.tms.event.api.beskjed.BeskjedEventService
import no.nav.tms.event.api.common.AzureTokenFetcher
import no.nav.tms.event.api.health.HealthService
import no.nav.tms.event.api.innboks.InnboksConsumer
import no.nav.tms.event.api.innboks.InnboksEventService
import no.nav.tms.event.api.oppgave.OppgaveConsumer
import no.nav.tms.event.api.oppgave.OppgaveEventService
import no.nav.tms.token.support.azure.exchange.AzureServiceBuilder
import java.net.URL

class ApplicationContext {

    val environment = Environment()

    val httpClient = HttpClientBuilder.build()
    val healthService = HealthService(this)

    val azureService = AzureServiceBuilder.buildAzureService(enableDefaultProxy = true)
    val azureTokenFetcher = AzureTokenFetcher(azureService, environment.eventHandlerClientId)

    val beskjedConsumer = BeskjedConsumer(httpClient, URL(environment.eventHandlerUrl))
    val beskjedEventService = BeskjedEventService(beskjedConsumer, azureTokenFetcher)

    val oppgaveConsumer = OppgaveConsumer(httpClient, URL(environment.eventHandlerUrl))
    val oppgaveEventService = OppgaveEventService(oppgaveConsumer, azureTokenFetcher)

    val innboksConsumer = InnboksConsumer(httpClient, URL(environment.eventHandlerUrl))
    val innboksEventService = InnboksEventService(innboksConsumer, azureTokenFetcher)
}
