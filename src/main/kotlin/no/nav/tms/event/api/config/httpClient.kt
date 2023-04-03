package no.nav.tms.event.api.config

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json

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
