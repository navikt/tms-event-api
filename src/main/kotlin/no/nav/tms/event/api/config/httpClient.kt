package no.nav.tms.event.api.config

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.apache.Apache
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.timeout
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.tms.event.api.varsel.Varsel
import no.nav.tms.token.support.azure.validation.AzureHeader
import java.net.URL

object HttpClientBuilder {

    fun build(): HttpClient {
        return HttpClient(Apache) {
            install(ContentNegotiation) {
                json(jsonConfig())
            }
            install(HttpTimeout)
        }
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
