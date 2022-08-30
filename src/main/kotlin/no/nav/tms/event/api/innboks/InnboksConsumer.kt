package no.nav.tms.event.api.innboks

import io.ktor.client.HttpClient
import no.nav.tms.event.api.common.AzureToken
import no.nav.tms.event.api.common.retryOnConnectionLost
import no.nav.tms.event.api.config.getWithAzureAndFnr
import java.net.URL

class InnboksConsumer(
    private val client: HttpClient,
    eventHandlerBaseURL: URL
) {

    private val activeEventsEndpoint = URL("$eventHandlerBaseURL/fetch/modia/innboks/aktive")
    private val inactiveEventsEndpoint = URL("$eventHandlerBaseURL/fetch/modia/innboks/inaktive")
    private val allEventsEndpoint = URL("$eventHandlerBaseURL/fetch/modia/innboks/all")

    suspend fun getActiveEvents(accessToken: AzureToken, fnr: String): List<Innboks> {
        return retryOnConnectionLost {
            getExternalEvents(accessToken, fnr, activeEventsEndpoint)
        }
    }

    suspend fun getInactiveEvents(accessToken: AzureToken, fnr: String): List<Innboks> {
        return retryOnConnectionLost {
            getExternalEvents(accessToken, fnr, inactiveEventsEndpoint)
        }
    }

    suspend fun getAllEvents(accessToken: AzureToken, fnr: String): List<Innboks> {
        return retryOnConnectionLost {
            getExternalEvents(accessToken, fnr, allEventsEndpoint)
        }
    }

    private suspend fun getExternalEvents(accessToken: AzureToken, fnr: String, completePathToEndpoint: URL): List<Innboks> {
        return client.getWithAzureAndFnr(completePathToEndpoint, accessToken, fnr)
    }
}
