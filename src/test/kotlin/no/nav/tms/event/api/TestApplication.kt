package no.nav.tms.event.api

import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockk
import nav.no.tms.common.testutils.RouteProvider
import no.nav.tms.event.api.config.AzureTokenFetcher
import no.nav.tms.event.api.config.jsonConfig
import no.nav.tms.event.api.varsel.LegacyVarsel
import no.nav.tms.event.api.varsel.VarselReader
import no.nav.tms.token.support.azure.validation.mock.azureMock

internal val azureMockToken = "TokenSmoken"

fun ApplicationTestBuilder.eventApiSetup(
    varselAuthorotyUrl: String,
    block: ApplicationTestBuilder.() -> Unit = {},
) = run {
    val applicationClient =
        createClient {
            install(ContentNegotiation) {
                json(jsonConfig())
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 2000
            }
        }
    application {
        val tokenFetchMock =
            mockk<AzureTokenFetcher>(relaxed = true).also {
                coEvery {
                    it.fetchTokenForVarselAuthority()
                } returns azureMockToken
            }
        api(
            varselReader =
                VarselReader(
                    azureTokenFetcher = tokenFetchMock,
                    client = applicationClient,
                    varselAuthorityUrl = varselAuthorotyUrl,
                ),
            httpClient = applicationClient,
            authConfig = {
                authentication {
                    azureMock {
                        alwaysAuthenticated = true
                        setAsDefault = true
                    }
                }
            },
        )
    }

    block()
}

class VarselRouteProvider(
    type: String,
    endpoint: String,
    fnrHeaderShouldBe: String,
    private val responseBody: String,
    statusCode: HttpStatusCode = OK,
) :
    RouteProvider(
            statusCode = statusCode,
            path = "/$type/$endpoint",
            routeMethodFunction = Routing::get,
            assert = { call ->
                if (call.request.headers["ident"] != fnrHeaderShouldBe) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            },
        ) {
    override fun content(): String = responseBody
}

class ErrorRouteProvider(
    private val endpoint: String,
    statusCode: HttpStatusCode,
) : RouteProvider(endpoint, Routing::get, statusCode) {
    override fun content(): String = "Error from $endpoint"
}

internal operator fun LegacyVarsel.times(size: Int): List<LegacyVarsel> =
    mutableListOf<LegacyVarsel>().also { list ->
        for (i in 1..size) {
            list.add(this)
        }
    }
