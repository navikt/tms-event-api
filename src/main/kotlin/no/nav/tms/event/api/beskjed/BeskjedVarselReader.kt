package no.nav.tms.event.api.beskjed

import io.ktor.client.HttpClient
import no.nav.tms.event.api.common.AzureTokenFetcher
import no.nav.tms.event.api.common.retryOnConnectionLost
import no.nav.tms.event.api.config.getWithAzureAndFnr
import java.net.URL

class BeskjedVarselReader(
    private val azureTokenFetcher: AzureTokenFetcher,
    private val client: HttpClient,
    eventHandlerBaseURL: String
) {

    private val aktiveVarslerEndpoint = URL("$eventHandlerBaseURL/fetch/modia/beskjed/aktive")
    private val inaktiveVarslerEndpoint = URL("$eventHandlerBaseURL/fetch/modia/beskjed/inaktive")
    private val alleVarslerEndpoint = URL("$eventHandlerBaseURL/fetch/modia/beskjed/all")
    suspend fun aktiveVarsler(fnr: String): List<BeskjedDTO> = getEksterneVarsler(fnr, aktiveVarslerEndpoint)

    suspend fun inaktiveVarsler(fnr: String): List<BeskjedDTO> = getEksterneVarsler(fnr, inaktiveVarslerEndpoint)

    suspend fun alleVarsler(fnr: String): List<BeskjedDTO> = getEksterneVarsler(fnr, alleVarslerEndpoint)

    private suspend fun getEksterneVarsler(
        fnr: String,
        completePathToEndpoint: URL
    ): List<BeskjedDTO> {
        val azureToken = azureTokenFetcher.fetchTokenForEventHandler()
        return retryOnConnectionLost<List<Beskjed>> {
            client.getWithAzureAndFnr(completePathToEndpoint, azureToken, fnr)
        }.map { beskjed -> beskjed.toDTO() }
    }
}
