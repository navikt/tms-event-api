package no.nav.tms.event.api.beskjed

import no.nav.tms.event.api.common.AzureTokenFetcher
import no.nav.tms.event.api.common.User

class BeskjedEventService(
    private val beskjedConsumer: BeskjedConsumer,
    private val azureTokenFetcher: AzureTokenFetcher
) {

    suspend fun getActiveCachedEventsForUser(bruker: User): List<BeskjedDTO> {
        val azureToken = azureTokenFetcher.fetchTokenForEventHandler()

        val beskjedList = beskjedConsumer.getActiveEvents(azureToken, bruker.fodselsnummer)

        return BeskjedTransformer.toBeskjedDTO(beskjedList)
    }

    suspend fun getInactiveCachedEventsForUser(bruker: User): List<BeskjedDTO> {
        val azureToken = azureTokenFetcher.fetchTokenForEventHandler()

        val beskjedList = beskjedConsumer.getInactiveEvents(azureToken, bruker.fodselsnummer)

        return BeskjedTransformer.toBeskjedDTO(beskjedList)
    }

    suspend fun getAllCachedEventsForUser(bruker: User): List<BeskjedDTO> {
        val azureToken = azureTokenFetcher.fetchTokenForEventHandler()

        val beskjedList = beskjedConsumer.getAllEvents(azureToken, bruker.fodselsnummer)

        return BeskjedTransformer.toBeskjedDTO(beskjedList)
    }
}
