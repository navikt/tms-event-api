package no.nav.tms.event.api.varsel

import io.ktor.client.HttpClient
import no.nav.tms.event.api.config.AzureTokenFetcher
import no.nav.tms.event.api.config.getWithAzureAndFnr
import no.nav.tms.event.api.config.retryOnConnectionLost
import java.net.URL

class VarselReader(
    private val azureTokenFetcher: AzureTokenFetcher,
    private val client: HttpClient,
    private val eventHandlerBaseURL: String
) {

    suspend fun fetchVarsel(
        fnr: String,
        varselPath: String
    ): List<VarselDTO> {
        val completePathToEndpoint = URL("$eventHandlerBaseURL/$varselPath")
        val azureToken = azureTokenFetcher.fetchTokenForEventHandler()
        return retryOnConnectionLost<List<Varsel>> {
            client.getWithAzureAndFnr(completePathToEndpoint, azureToken, fnr)
        }.map { beskjed -> beskjed.toDTO() }
    }
}
