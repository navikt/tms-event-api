package no.nav.tms.event.api.varsel

import io.ktor.client.HttpClient
import no.nav.tms.event.api.config.AzureTokenFetcher
import no.nav.tms.event.api.config.getWithAzureAndFnr
import java.net.URL

class VarselReader(
    private val azureTokenFetcher: AzureTokenFetcher,
    private val client: HttpClient,
    private val eventHandlerBaseURL: String
) {

    suspend fun fetchVarsel(
        fnr: String,
        varselPath: String
    ): List<Varsel> {
        val completePathToEndpoint = URL("$eventHandlerBaseURL/$varselPath")
        val azureToken = azureTokenFetcher.fetchTokenForEventHandler()

        return client.getWithAzureAndFnr(completePathToEndpoint, azureToken, fnr)
    }
}
