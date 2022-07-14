package no.nav.tms.event.api.innboks

import no.nav.tms.event.api.common.AzureTokenFetcher
import no.nav.tms.event.api.common.User
import no.nav.tms.event.api.innboks.Innboks
import no.nav.tms.event.api.innboks.InnboksDTO

class InnboksEventService(
    private val innboksConsumer: InnboksConsumer,
    private val azureTokenFetcher: AzureTokenFetcher
) {

    suspend fun getActiveCachedEventsForUser(bruker: User): List<InnboksDTO> {
        val azureToken = azureTokenFetcher.fetchTokenForEventHandler()

        val innboksList = innboksConsumer.getActiveEvents(azureToken, bruker.fodselsnummer)

        return InnboksTransformer.toInnboksDTO(innboksList)
    }

    suspend fun getInactiveCachedEventsForUser(bruker: User): List<InnboksDTO> {
        val azureToken = azureTokenFetcher.fetchTokenForEventHandler()

        val innboksList = innboksConsumer.getInactiveEvents(azureToken, bruker.fodselsnummer)

        return InnboksTransformer.toInnboksDTO(innboksList)
    }

    suspend fun getAllCachedEventsForUser(bruker: User): List<InnboksDTO> {
        val azureToken = azureTokenFetcher.fetchTokenForEventHandler()

        val innboksList = innboksConsumer.getAllEvents(azureToken, bruker.fodselsnummer)

        return InnboksTransformer.toInnboksDTO(innboksList)
    }
}
