package no.nav.tms.event.api.innboks

import no.nav.tms.event.api.common.AzureTokenFetcher
import no.nav.tms.event.api.common.User

class InnboksEventService(
    private val innboksConsumer: InnboksConsumer,
    private val azureTokenFetcher: AzureTokenFetcher
) {

    suspend fun getActiveCachedEventsForUser(bruker: User): List<Innboks> {
        val azureToken = azureTokenFetcher.fetchTokenForEventHandler()

        return innboksConsumer.getActiveEvents(azureToken, bruker.fodselsnummer)
    }

    suspend fun getInactiveCachedEventsForUser(bruker: User): List<Innboks> {
        val azureToken = azureTokenFetcher.fetchTokenForEventHandler()

        return innboksConsumer.getInactiveEvents(azureToken, bruker.fodselsnummer)
    }

    suspend fun getAllCachedEventsForUser(bruker: User): List<Innboks> {
        val azureToken = azureTokenFetcher.fetchTokenForEventHandler()

        return innboksConsumer.getAllEvents(azureToken, bruker.fodselsnummer)
    }
}
