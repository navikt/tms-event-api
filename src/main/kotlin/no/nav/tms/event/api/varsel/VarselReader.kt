package no.nav.tms.event.api.varsel

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.tms.event.api.config.AzureTokenFetcher
import no.nav.tms.token.support.azure.validation.AzureHeader
import java.net.URL

class VarselReader(
    private val azureTokenFetcher: AzureTokenFetcher,
    private val client: HttpClient,
    private val eventHandlerBaseURL: String,
) {
    suspend fun fetchVarsel(
        fnr: String,
        varselPath: String,
    ): List<Varsel> {
        val completePathToEndpoint = URL("$eventHandlerBaseURL/$varselPath")
        val azureToken = azureTokenFetcher.fetchTokenForEventHandler()
        return client.getWithAzureAndFnr(completePathToEndpoint, azureToken, fnr)
    }
}

suspend inline fun HttpClient.getWithAzureAndFnr(url: URL, accessToken: String, fnr: String): List<Varsel> =
    withContext(Dispatchers.IO) {
        request {
            url(url)
            method = HttpMethod.Get
            accept(ContentType.Application.Json)
            header(AzureHeader.Authorization, "Bearer $accessToken")
            header("fodselsnummer", fnr)
            timeout {
                socketTimeoutMillis = 30000
                connectTimeoutMillis = 10000
                requestTimeoutMillis = 40000
            }
        }
    }.body()
