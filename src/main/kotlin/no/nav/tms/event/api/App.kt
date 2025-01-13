package no.nav.tms.event.api

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.tms.common.metrics.installTmsMicrometerMetrics
import no.nav.tms.common.observability.ApiMdc
import no.nav.tms.common.util.config.StringEnvVar
import no.nav.tms.event.api.config.AzureTokenFetcher
import no.nav.tms.event.api.config.HttpClientBuilder
import no.nav.tms.event.api.config.healthApi
import no.nav.tms.event.api.config.jsonConfig
import no.nav.tms.event.api.varsel.*
import no.nav.tms.token.support.azure.exchange.AzureServiceBuilder
import no.nav.tms.token.support.azure.validation.azure

fun main() {
    val varselAuthorityUrl = "http://tms-varsel-authority"
    val varselAuthorityClientId: String = StringEnvVar.getEnvVar("VARSEL_AUTHORITY_CLIENT_ID")

    val httpClient = HttpClientBuilder.build()
    val azureService = AzureServiceBuilder.buildAzureService(enableDefaultProxy = true)
    val azureTokenFetcher = AzureTokenFetcher(azureService, varselAuthorityClientId)
    val varselReader =
        VarselReader(
            azureTokenFetcher = azureTokenFetcher,
            client = httpClient,
            varselAuthorityUrl = varselAuthorityUrl,
        )

    embeddedServer(
        factory = Netty,
        configure = {
            connector {
                port = 8080
            }
        },
        module = {
            rootPath = "tms-event-api"

            api(
                authConfig = authConfigBuilder(),
                httpClient = httpClient,
                varselReader = varselReader,
            )
        },
    ).start(wait = true)
}

fun Application.api(
    varselReader: VarselReader,
    httpClient: HttpClient,
    authConfig: Application.() -> Unit,
) {
    val log = KotlinLogging.logger {}
    val securelog = KotlinLogging.logger("secureLog")

    install(DefaultHeaders)
    authConfig()
    install(ContentNegotiation) {
        json(jsonConfig())
    }

    installTmsMicrometerMetrics {
        setupMetricsRoute = true
        installMicrometerPlugin = true
    }
    install(ApiMdc)

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            when (cause) {
                is IllegalArgumentException -> {
                    log.error { cause.message }
                    call.respond(HttpStatusCode.BadRequest)
                }
                is VarselFetchError -> {
                    log.error { "Kall mot ${cause.url} feiler med staus ${cause.statusCode}" }
                    call.respond(HttpStatusCode.ServiceUnavailable)
                }

                else -> {
                    log.error { "Kall til ${call.request.uri} feilet: ${cause.message}" }
                    securelog.error(cause) { "Kall til ${call.request.uri} feilet: \n ${cause.stackTrace}" }
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
        }
    }

    routing {
        healthApi()
        authenticate {
            legavyVarselApi(varselReader)
            varselApi(varselReader)
        }
    }

    configureShutdownHook(httpClient)
}

private fun Application.configureShutdownHook(httpClient: HttpClient) {
    monitor.subscribe(ApplicationStopping) {
        httpClient.close()
    }
}

private fun authConfigBuilder(): Application.() -> Unit =
    {
        authentication {
            azure {
                setAsDefault = true
            }
        }
    }
