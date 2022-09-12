package no.nav.tms.event.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.testing.TestApplicationBuilder
import io.mockk.mockk
import no.nav.tms.event.api.config.AzureTokenFetcher
import no.nav.tms.event.api.config.jsonConfig
import no.nav.tms.event.api.varsel.VarselDTO
import no.nav.tms.event.api.varsel.VarselReader
import no.nav.tms.token.support.authentication.installer.mock.installMockedAuthenticators
import no.nav.tms.token.support.tokenx.validation.mock.SecurityLevel

fun TestApplicationBuilder.mockApi(
    authConfig: Application.() -> Unit = mockAuthBuilder(),
    httpClient: HttpClient = mockk(relaxed = true),
    azureTokenFetcher: AzureTokenFetcher
) {
    return application {
        api(
            authConfig = authConfig,
            httpClient = httpClient,
            varselReader = VarselReader(
                azureTokenFetcher = azureTokenFetcher,
                client = httpClient,
                eventHandlerBaseURL = "https://test.noe"
            )
        )
    }
}

fun mockAuthBuilder(): Application.() -> Unit = {
    installMockedAuthenticators {
        installTokenXAuthMock {
            setAsDefault = true

            alwaysAuthenticated = true
            staticUserPid = "123"
            staticSecurityLevel = SecurityLevel.LEVEL_4
        }
        installAzureAuthMock { }
    }
}

fun mockClient(mockContent: String) = HttpClient(
    MockEngine {
        respond(
            content = mockContent,
            status = HttpStatusCode.OK,
            headersOf(HttpHeaders.ContentType, "application/json")
        )
    }

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
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        } else {
            respond(
                content = "ukjent url",
                status = HttpStatusCode.NotFound,
            )
        }
    }

) {
    install(ContentNegotiation) {
        json(jsonConfig())
    }
    install(HttpTimeout)
}

internal operator fun VarselDTO.times(size: Int): List<VarselDTO> = mutableListOf<VarselDTO>().also { list ->
    for (i in 1..size) {
        list.add(this)
    }
}
