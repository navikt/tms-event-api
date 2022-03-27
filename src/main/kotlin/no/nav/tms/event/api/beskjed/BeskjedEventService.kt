package no.nav.tms.event.api.beskjed

import no.nav.tms.event.api.common.AzureTokenFetcher
import no.nav.tms.event.api.common.User

class BeskjedEventService(
    private val beskjedConsumer: BeskjedConsumer,
    private val azureTokenFetcher: AzureTokenFetcher
) {

    suspend fun getActiveCachedEventsForUser(bruker: User): List<Beskjed> {
        val azureToken = azureTokenFetcher.fetchTokenForEventHandler()

        return beskjedConsumer.getActiveEvents(azureToken, bruker.fodselsnummer)
    }

    suspend fun getInactiveCachedEventsForUser(bruker: User): List<Beskjed> {
        val azureToken = azureTokenFetcher.fetchTokenForEventHandler()

        return beskjedConsumer.getInactiveEvents(azureToken, bruker.fodselsnummer)
    }

    suspend fun getAllCachedEventsForUser(bruker: User): List<Beskjed> {
        val azureToken = azureTokenFetcher.fetchTokenForEventHandler()

        return beskjedConsumer.getAllEvents(azureToken, bruker.fodselsnummer)
    }
}
