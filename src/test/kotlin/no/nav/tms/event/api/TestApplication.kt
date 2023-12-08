package no.nav.tms.event.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.tms.event.api.config.AzureTokenFetcher
import no.nav.tms.event.api.config.jsonConfig
import no.nav.tms.event.api.varsel.LegacyVarsel
import no.nav.tms.event.api.varsel.VarselReader
import no.nav.tms.token.support.azure.validation.mock.azureMock

internal val tokenFetchMock = mockk<AzureTokenFetcher>(relaxed = true)
internal val azureMockToken = "TokenSmoken"
internal val testHostUrl = "https://www.test.no"

fun ApplicationTestBuilder.eventApiSetup(
    block: ApplicationTestBuilder.() -> Unit,
) = run {
    val applicationClient = createClient {
        install(ContentNegotiation) {
            json(jsonConfig())
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 2000
        }
    }
    application {
        val tokenFetchMock = mockk<AzureTokenFetcher>(relaxed = true).also {
            coEvery {
                it.fetchTokenForVarselAuthority()
            } returns azureMockToken
        }
        api(
            varselReader = VarselReader(
                azureTokenFetcher = tokenFetchMock,
                client = applicationClient,
                varselAuthorityUrl = testHostUrl,
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

fun ExternalServicesBuilder.tmsAuthoritySetup(
    endpointPostfix: String,
    mockresponse: String,
    fnrHeaderShouldBe: String,
) {
    hosts(testHostUrl) {
        install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
            jsonConfig()
        }
        routing {
            get("/beskjed/$endpointPostfix") {
                if (call.request.headers["ident"] != fnrHeaderShouldBe) {
                    call.respond(HttpStatusCode.BadRequest)
                }
                call.respondText(
                    text = mockresponse,
                    contentType = ContentType.Application.Json,
                )
            }
            get("innboks/$endpointPostfix") {
                if (call.request.headers["ident"] != fnrHeaderShouldBe) {
                    call.respond(HttpStatusCode.BadRequest)
                }
                call.respondText(
                    text = mockresponse,
                    contentType = ContentType.Application.Json,
                )
            }
            get("oppgave/$endpointPostfix") {
                if (call.request.headers["ident"] != fnrHeaderShouldBe) {
                    call.respond(HttpStatusCode.BadRequest)
                }
                call.respondText(
                    text = mockresponse,
                    contentType = ContentType.Application.Json,
                )
            }
        }
    }
}

fun TestApplicationBuilder.mockApi(
    authConfig: Application.() -> Unit = mockAuthBuilder(),
    httpClient: HttpClient = mockk(relaxed = true),
    azureTokenFetcher: AzureTokenFetcher,
) {
    environment {
        rootPath = "tms-event-api"
    }
    return application {
        api(
            authConfig = authConfig,
            httpClient = httpClient,
            varselReader = VarselReader(
                azureTokenFetcher = azureTokenFetcher,
                client = httpClient,
                varselAuthorityUrl = "https://test.noe",
            ),
        )
    }
}

fun mockAuthBuilder(): Application.() -> Unit = {
    authentication {
        azureMock {
            setAsDefault = true
            alwaysAuthenticated = true
        }
    }
}

fun mockClient(mockContent: String) = HttpClient(
    MockEngine {
        respond(
            content = mockContent,
            status = HttpStatusCode.OK,
            headersOf(HttpHeaders.ContentType, "application/json"),
        )
    },

) {
    install(ContentNegotiation) {
        json(jsonConfig())
    }
    install(HttpTimeout)
}

fun mockClientWithEndpointValidation(endpointValidation: String, mockContent: String) = HttpClient(
    MockEngine { request ->
        if (request.url.toString().contains(endpointValidation)) {
            respond(
                content = mockContent,
                status = HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json"),
            )
        } else {
            respond(
                content = "ukjent url",
                status = HttpStatusCode.NotFound,
            )
        }
    },

) {
    install(ContentNegotiation) {
        json(jsonConfig())
    }
    install(HttpTimeout)
}

internal operator fun LegacyVarsel.times(size: Int): List<LegacyVarsel> = mutableListOf<LegacyVarsel>().also { list ->
    for (i in 1..size) {
        list.add(this)
    }
}
