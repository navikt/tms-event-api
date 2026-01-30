package no.nav.tms.event.api.varsel

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import no.nav.tms.event.api.config.AzureTokenFetcher
import no.nav.tms.token.support.azure.validation.AzureHeader
import java.net.URI
import java.net.URL

class VarselReader(
    private val azureTokenFetcher: AzureTokenFetcher,
    private val client: HttpClient,
    private val varselAuthorityUrl: String,
) {
    suspend fun fetchVarsel(
        fnr: String,
        varselPath: String,
    ): List<DetaljertVarsel> {
        val completePathToEndpoint = URI.create("$varselAuthorityUrl/$varselPath").toURL()
        val azureToken = azureTokenFetcher.fetchTokenForVarselAuthority()
        return client.getWithAzureAndFnr(completePathToEndpoint, azureToken, fnr)
    }
}

// TODO: fiks feilhåndtering på feilbeskjeder
suspend fun HttpClient.getWithAzureAndFnr(
    url: URL,
    accessToken: String,
    fnr: String,
): List<DetaljertVarsel> =
    withContext(Dispatchers.IO) {
        post {
            url(url)
            accept(ContentType.Application.Json)
            header(AzureHeader.Authorization, "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(identBody(fnr))
            timeout {
                socketTimeoutMillis = 30000
                connectTimeoutMillis = 10000
                requestTimeoutMillis = 40000
            }
        }
    }.let {
        if (it.status != HttpStatusCode.OK) throw VarselFetchError(it.request.url, it.status)
        it.body()
    }

private fun identBody(
    ident: String
) = """
{
    "ident": "$ident"
} 
"""

class VarselFetchError(val url: Url, val statusCode: HttpStatusCode) : Exception()
