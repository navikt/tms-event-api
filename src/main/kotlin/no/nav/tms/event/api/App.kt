package no.nav.tms.event.api

import io.ktor.client.HttpClient
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.personbruker.dittnav.common.util.config.StringEnvVar
import no.nav.tms.event.api.config.AzureTokenFetcher
import no.nav.tms.event.api.config.HttpClientBuilder
import no.nav.tms.event.api.config.healthApi
import no.nav.tms.event.api.config.jsonConfig
import no.nav.tms.event.api.varsel.VarselReader
import no.nav.tms.event.api.varsel.beskjedApi
import no.nav.tms.event.api.varsel.innboksApi
import no.nav.tms.event.api.varsel.oppgaveApi
import no.nav.tms.token.support.azure.exchange.AzureServiceBuilder
import no.nav.tms.token.support.azure.validation.installAzureAuth

fun main() {
    val eventHandlerUrl: String = StringEnvVar.getEnvVar("EVENT_HANDLER_URL")
    val eventHandlerClientId: String = StringEnvVar.getEnvVar("EVENT_HANDLER_CLIENT_ID")

    val httpClient = HttpClientBuilder.build()
    val azureService = AzureServiceBuilder.buildAzureService(enableDefaultProxy = true)
    val azureTokenFetcher = AzureTokenFetcher(azureService, eventHandlerClientId)
    val varselReader = VarselReader(
        azureTokenFetcher = azureTokenFetcher,
        client = httpClient,
        eventHandlerBaseURL = eventHandlerUrl
    )

    embeddedServer(Netty, port = 8080) {
        api(
            // authConfig = authConfigBuilder(),
            httpClient = httpClient,
            varselReader = varselReader
        )
    }.start(wait = true)
}

fun Application.api(
    varselReader: VarselReader,
    httpClient: HttpClient,
    //  authConfig: Application.() -> Unit = {}
) {
    val prometheusMeterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    install(DefaultHeaders)
    // authConfig()
    install(ContentNegotiation) {
        json(jsonConfig())
    }
    install(MicrometerMetrics) {
        registry = prometheusMeterRegistry
    }

    routing {
        route("/tms-event-api") {
            healthApi(prometheusMeterRegistry)
            //      authenticate {
            oppgaveApi(varselReader)
            beskjedApi(varselReader)
            innboksApi(varselReader)
            //       }
        }
    }

    configureShutdownHook(httpClient)
}

private fun Application.configureShutdownHook(httpClient: HttpClient) {
    environment.monitor.subscribe(ApplicationStopping) {
        httpClient.close()
    }
}

private fun authConfigBuilder(): Application.() -> Unit = {
    installAzureAuth {
        setAsDefault = true
    }
}
