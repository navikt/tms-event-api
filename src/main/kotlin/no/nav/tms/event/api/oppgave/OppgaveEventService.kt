package no.nav.tms.event.api.oppgave

import no.nav.tms.event.api.common.AzureTokenFetcher
import no.nav.tms.event.api.common.User

class OppgaveEventService(
    private val oppgaveConsumer: OppgaveConsumer,
    private val azureTokenFetcher: AzureTokenFetcher
) {

    suspend fun getActiveCachedEventsForUser(bruker: User): List<Oppgave> {
        val azureToken = azureTokenFetcher.fetchTokenForEventHandler()

        return oppgaveConsumer.getActiveEvents(azureToken, bruker.fodselsnummer)
    }

    suspend fun getInactiveCachedEventsForUser(bruker: User): List<Oppgave> {
        val azureToken = azureTokenFetcher.fetchTokenForEventHandler()

        return oppgaveConsumer.getInactiveEvents(azureToken, bruker.fodselsnummer)
    }

    suspend fun getAllCachedEventsForUser(bruker: User): List<Oppgave> {
        val azureToken = azureTokenFetcher.fetchTokenForEventHandler()

        return oppgaveConsumer.getAllEvents(azureToken, bruker.fodselsnummer)
    }
}
