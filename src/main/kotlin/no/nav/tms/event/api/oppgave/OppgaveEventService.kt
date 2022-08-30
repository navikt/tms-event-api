package no.nav.tms.event.api.oppgave

import no.nav.tms.event.api.common.AzureTokenFetcher

class OppgaveEventService(
    private val oppgaveConsumer: OppgaveConsumer,
    private val azureTokenFetcher: AzureTokenFetcher
) {

    suspend fun getActiveCachedEventsForUser(fnr: String): List<OppgaveDTO> {
        val azureToken = azureTokenFetcher.fetchTokenForEventHandler()

        return oppgaveConsumer.getActiveEvents(azureToken, fnr).let {
            OppgaveTransformer.toOppgaveDTO(it)
        }
    }

    suspend fun getInactiveCachedEventsForUser(fnr: String): List<OppgaveDTO> {
        val azureToken = azureTokenFetcher.fetchTokenForEventHandler()

        return oppgaveConsumer.getInactiveEvents(azureToken, fnr).let {
            OppgaveTransformer.toOppgaveDTO(it)
        }
    }

    suspend fun getAllCachedEventsForUser(fnr: String): List<OppgaveDTO> {
        val azureToken = azureTokenFetcher.fetchTokenForEventHandler()

        return oppgaveConsumer.getAllEvents(azureToken, fnr).let {
            OppgaveTransformer.toOppgaveDTO(it)
        }
    }
}
