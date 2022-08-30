package no.nav.tms.event.api.innboks

import no.nav.tms.event.api.common.AzureTokenFetcher

class InnboksEventService(
    private val innboksConsumer: InnboksConsumer,
    private val azureTokenFetcher: AzureTokenFetcher
) {

    suspend fun getActiveCachedEventsForUser(fnr: String): List<InnboksDTO> {
        val azureToken = azureTokenFetcher.fetchTokenForEventHandler()

        return innboksConsumer.getActiveEvents(azureToken, fnr).let {
            InnboksTransformer.toInnboksDTO(it)
        }
    }

    suspend fun getInactiveCachedEventsForUser(fnr: String): List<InnboksDTO> {
        val azureToken = azureTokenFetcher.fetchTokenForEventHandler()

        return innboksConsumer.getInactiveEvents(azureToken, fnr).let {
            InnboksTransformer.toInnboksDTO(it)
        }
    }

    suspend fun getAllCachedEventsForUser(fnr: String): List<InnboksDTO> {
        val azureToken = azureTokenFetcher.fetchTokenForEventHandler()

        return innboksConsumer.getAllEvents(azureToken, fnr).let {
            InnboksTransformer.toInnboksDTO(it)
        }
    }
}
