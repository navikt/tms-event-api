package no.nav.tms.event.api.beskjed

import io.ktor.client.*
import no.nav.tms.event.api.common.AzureToken
import no.nav.tms.event.api.common.retryOnConnectionLost
import no.nav.tms.event.api.config.getWithAzureAndFnr
import java.net.URL

class BeskjedConsumer(
    private val client: HttpClient,
    eventHandlerBaseURL: URL
) {

    private val activeEventsEndpoint = URL("$eventHandlerBaseURL/fetch/modia/beskjed/aktive")
    private val inactiveEventsEndpoint = URL("$eventHandlerBaseURL/fetch/modia/beskjed/inaktive")
    private val allEventsEndpoint = URL("$eventHandlerBaseURL/fetch/modia/beskjed/all")

    suspend fun getActiveEvents(accessToken: AzureToken, fnr: String): List<Beskjed> {
        return retryOnConnectionLost {
            getExternalEvents(accessToken, fnr, activeEventsEndpoint)
        }
    }

    suspend fun getInactiveEvents(accessToken: AzureToken, fnr: String): List<Beskjed> {
        return retryOnConnectionLost {
            getExternalEvents(accessToken, fnr, inactiveEventsEndpoint)
        }
    }

    suspend fun getAllEvents(accessToken: AzureToken, fnr: String): List<Beskjed> {
        return retryOnConnectionLost {
            getExternalEvents(accessToken, fnr, allEventsEndpoint)
        }
    }

    private suspend fun getExternalEvents(accessToken: AzureToken, fnr: String, completePathToEndpoint: URL): List<Beskjed> {
        return client.getWithAzureAndFnr(completePathToEndpoint, accessToken, fnr)
    }
}

