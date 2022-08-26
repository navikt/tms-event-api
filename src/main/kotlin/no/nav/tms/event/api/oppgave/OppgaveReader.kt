package no.nav.tms.event.api.oppgave

import io.ktor.client.HttpClient
import no.nav.tms.event.api.common.AzureToken
import no.nav.tms.event.api.common.AzureTokenFetcher
import no.nav.tms.event.api.common.retryOnConnectionLost
import no.nav.tms.event.api.config.getWithAzureAndFnr
import no.nav.tms.event.api.oppgave.Oppgave.Companion.toDTO
import java.net.URL

class OppgaveReader(
    private val azureTokenFetcher: AzureTokenFetcher,
    private val client: HttpClient,
    eventHandlerBaseURL: String
) {
    private val aktiveVarslerEndpoint = URL("$eventHandlerBaseURL/fetch/modia/oppgave/aktive")
    private val inaktiveVarslerEndpoint = URL("$eventHandlerBaseURL/fetch/modia/oppgave/inaktive")
    private val alleVarslerEndpoint = URL("$eventHandlerBaseURL/fetch/modia/oppgave/all")
    suspend fun aktiveVarsler(fnr: String): List<OppgaveDTO> {
        val azureToken = azureTokenFetcher.fetchTokenForEventHandler()

        return retryOnConnectionLost {
            getExternalEvents(azureToken, fnr, aktiveVarslerEndpoint)
        }.toDTO()
    }

    suspend fun inaktiveVarsler(fnr: String): List<OppgaveDTO> {
        val azureToken = azureTokenFetcher.fetchTokenForEventHandler()

        return retryOnConnectionLost {
            getExternalEvents(azureToken, fnr, inaktiveVarslerEndpoint)
        }.toDTO()
    }

    suspend fun alleVarsler(fnr: String): List<OppgaveDTO> {
        val azureToken = azureTokenFetcher.fetchTokenForEventHandler()

        return retryOnConnectionLost {
            getExternalEvents(azureToken, fnr, alleVarslerEndpoint)
        }.toDTO()
    }

    private suspend fun getExternalEvents(
        accessToken: AzureToken,
        fnr: String,
        completePathToEndpoint: URL
    ): List<Oppgave> {
        return client.getWithAzureAndFnr(completePathToEndpoint, accessToken, fnr)
    }
}
