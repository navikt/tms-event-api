package no.nav.tms.event.api.config

import io.ktor.client.HttpClient
import io.ktor.client.features.timeout
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.http.HttpMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.tms.event.api.common.AzureToken
import no.nav.tms.token.support.azure.exchange.service.AzureHeader
import java.net.URL

suspend inline fun <reified T> HttpClient.getWithAzureAndFnr(url: URL, accessToken: AzureToken, fnr: String): T = withContext(Dispatchers.IO) {
    request {
        url(url)
        method = HttpMethod.Get
        header(AzureHeader.Authorization, "Bearer ${accessToken.value}")
        header("fodselsnummer", fnr)
        timeout {
            socketTimeoutMillis = 30000
            connectTimeoutMillis = 10000
            requestTimeoutMillis = 40000
        }
    }
}
