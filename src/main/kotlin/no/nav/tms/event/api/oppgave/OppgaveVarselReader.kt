package no.nav.tms.event.api.oppgave

import io.ktor.client.HttpClient
import no.nav.tms.event.api.common.AzureTokenFetcher
import no.nav.tms.event.api.common.retryOnConnectionLost
import no.nav.tms.event.api.config.getWithAzureAndFnr
import java.net.URL

class OppgaveVarselReader(
    private val azureTokenFetcher: AzureTokenFetcher,
    private val client: HttpClient,
    eventHandlerBaseURL: String
) {
    private val aktiveVarslerEndpoint = URL("$eventHandlerBaseURL/fetch/modia/oppgave/aktive")
    private val inaktiveVarslerEndpoint = URL("$eventHandlerBaseURL/fetch/modia/oppgave/inaktive")
    private val alleVarslerEndpoint = URL("$eventHandlerBaseURL/fetch/modia/oppgave/all")
    suspend fun aktiveVarsler(fnr: String): List<OppgaveDTO> = getEksterneVarsler(fnr, aktiveVarslerEndpoint)
    suspend fun inaktiveVarsler(fnr: String): List<OppgaveDTO> = getEksterneVarsler(fnr, inaktiveVarslerEndpoint)
    suspend fun alleVarsler(fnr: String): List<OppgaveDTO> = getEksterneVarsler(fnr, alleVarslerEndpoint)

    private suspend fun getEksterneVarsler(
        fnr: String,
        completePathToEndpoint: URL
    ): List<OppgaveDTO> {
        val azureToken = azureTokenFetcher.fetchTokenForEventHandler()
        return retryOnConnectionLost<List<Oppgave>> {
            client.getWithAzureAndFnr(completePathToEndpoint, azureToken, fnr)
        }.map { oppgave -> oppgave.toDTO() }
    }
}
