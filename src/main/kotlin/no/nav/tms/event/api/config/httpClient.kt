package no.nav.tms.event.api.config

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.apache.Apache
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.timeout
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.tms.event.api.varsel.Varsel
import no.nav.tms.token.support.azure.validation.AzureHeader
import org.apache.http.ConnectionClosedException
import java.net.SocketException
import java.net.URL

object HttpClientBuilder {

    fun build(): HttpClient {
        return HttpClient(Apache) {
            install(ContentNegotiation) {
                jsonConfig()
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
            header(AzureHeader.Authorization, "Bearer $accessToken")
            header("fodselsnummer", fnr)
            timeout {
                socketTimeoutMillis = 30000
                connectTimeoutMillis = 10000
                requestTimeoutMillis = 40000
            }
        }.also {
            log.info(it.bodyAsText())
        }.body()
    }

inline fun <reified T> retryOnConnectionLost(retries: Int = 3, outgoingCall: () -> T): T {
    var attempts = 0

    lateinit var lastError: Exception

    while (attempts < retries) {
        try {
            return outgoingCall()
        } catch (e: ConnectionClosedException) {
            attempts++
            lastError = e
        } catch (e: SocketException) {
            attempts++
            lastError = e
        }
    }

    throw ConnectionFailedException(
        "Klarte ikke hente data etter $attempts forsøk. Viser info for siste feil.",
        lastError
    )
}

class ConnectionFailedException(message: String, cause: Exception) : Exception(message, cause)
