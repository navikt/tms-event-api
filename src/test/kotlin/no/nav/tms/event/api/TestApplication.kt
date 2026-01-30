package no.nav.tms.event.api

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.withClue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.auth.*
import io.ktor.server.request.*
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
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

internal const val azureMockToken = "TokenSmoken"
internal val objectmapper = ObjectMapper()

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

fun TestApplicationBuilder.setupExternalVarselRoute(
    host: String,
    path: String,
    responseBody: String,
    statusCode: HttpStatusCode = OK,
    contentType: ContentType = ContentType.Application.Json,
    callPeek: suspend (RoutingCall) -> Unit = {},
) {
    externalServices {
        hosts(host) {
            routing {
                post(path) {
                    callPeek.invoke(call)
                    call.respondText(responseBody, status = statusCode, contentType = contentType)
                }
            }
        }
    }
}

fun TestApplicationBuilder.setupErrorVarselRoute(
    host: String,
    path: String,
    statusCode: HttpStatusCode = InternalServerError
) = setupExternalVarselRoute(
    host = host,
    path = path,
    responseBody = "Oh no",
    statusCode = statusCode,
    contentType = ContentType.Text.Plain,
)

internal operator fun LegacyVarsel.times(size: Int): List<LegacyVarsel> =
    mutableListOf<LegacyVarsel>().also { list ->
        for (i in 1..size) {
            list.add(this)
        }
    }

internal fun assertZonedDateTime(
    jsonNode: JsonNode?,
    expectedDate: ZonedDateTime?,
    key: String,
) {
    if (expectedDate != null) {
        val resultDate = ZonedDateTime.parse(jsonNode?.get(key)?.textValue()).truncatedTo(ChronoUnit.MINUTES)
        withClue("$key skal ikke være null") {
            resultDate.shouldNotBeNull()
        }
        resultDate.toString() shouldBe expectedDate.truncatedTo(ChronoUnit.MINUTES).toString()
    } else {
        jsonNode?.get(key)?.textValue() shouldBe null
    }
}
