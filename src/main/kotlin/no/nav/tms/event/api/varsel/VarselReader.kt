package no.nav.tms.event.api.varsel

import io.ktor.client.HttpClient
import no.nav.tms.event.api.config.AzureTokenFetcher
import no.nav.tms.event.api.config.getWithAzureAndFnr
import no.nav.tms.event.api.config.retryOnConnectionLost
import org.slf4j.LoggerFactory
import java.net.URL

class VarselReader(
    private val azureTokenFetcher: AzureTokenFetcher,
    private val client: HttpClient,
    private val eventHandlerBaseURL: String
) {
    val log = LoggerFactory.getLogger(VarselReader::class.java)

    suspend fun fetchVarsel(
        fnr: String,
        varselPath: String
    ): List<VarselDTO> {
        val completePathToEndpoint = URL("$eventHandlerBaseURL/$varselPath")
        log.debug("Forsøker å hente $varselPath fra $eventHandlerBaseURL")
        val azureToken = azureTokenFetcher.fetchTokenForEventHandler()
        log.debug("hentet token")
        return retryOnConnectionLost {
            client.getWithAzureAndFnr(completePathToEndpoint, azureToken, fnr)
        }.map { varsel -> varsel.toDTO() }
    }
}
