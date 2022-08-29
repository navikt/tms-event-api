package no.nav.tms.event.api.varsel

import io.ktor.client.HttpClient
import no.nav.tms.event.api.common.AzureTokenFetcher
import no.nav.tms.event.api.common.retryOnConnectionLost
import no.nav.tms.event.api.config.getWithAzureAndFnr
import java.net.URL

class VarselReader(private val azureTokenFetcher: AzureTokenFetcher, private val client: HttpClient) {

    suspend fun getEksterneVarsler(
        fnr: String,
        completePathToEndpoint: URL
    ): List<VarselDTO> {
        val azureToken = azureTokenFetcher.fetchTokenForEventHandler()
        return retryOnConnectionLost<List<Varsel>> {
            client.getWithAzureAndFnr(completePathToEndpoint, azureToken, fnr)
        }.map { beskjed -> beskjed.toDTO() }
    }
}
