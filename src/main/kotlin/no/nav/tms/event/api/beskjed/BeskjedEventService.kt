package no.nav.tms.event.api.beskjed

import no.nav.tms.event.api.common.AzureTokenFetcher

class BeskjedEventService(
    private val beskjedConsumer: BeskjedConsumer,
    private val azureTokenFetcher: AzureTokenFetcher
) {

    suspend fun getActiveCachedEventsForUser(fnr: String): List<BeskjedDTO> {
        val azureToken = azureTokenFetcher.fetchTokenForEventHandler()

        return beskjedConsumer.getActiveEvents(azureToken, fnr).let {
            BeskjedTransformer.toBeskjedDTO(it)
        }
    }

    suspend fun getInactiveCachedEventsForUser(fnr: String): List<BeskjedDTO> {
        val azureToken = azureTokenFetcher.fetchTokenForEventHandler()

        return beskjedConsumer.getInactiveEvents(azureToken, fnr).let {
            BeskjedTransformer.toBeskjedDTO(it)
        }
    }

    suspend fun getAllCachedEventsForUser(fnr: String): List<BeskjedDTO> {
        val azureToken = azureTokenFetcher.fetchTokenForEventHandler()

        return beskjedConsumer.getAllEvents(azureToken, fnr).let {
            BeskjedTransformer.toBeskjedDTO(it)
        }
    }
}
