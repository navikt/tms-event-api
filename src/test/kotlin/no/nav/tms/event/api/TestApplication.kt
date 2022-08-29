package no.nav.tms.event.api

import io.ktor.application.Application
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.features.HttpTimeout
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.mockk.mockk
import no.nav.tms.event.api.beskjed.BeskjedVarselReader
import no.nav.tms.event.api.config.api
import no.nav.tms.event.api.innboks.InnboksVarselReader
import no.nav.tms.event.api.oppgave.OppgaveVarselReader
import no.nav.tms.token.support.authentication.installer.mock.installMockedAuthenticators
import no.nav.tms.token.support.tokenx.validation.mock.SecurityLevel

fun mockApi(
    authConfig: Application.() -> Unit = mockAuthBuilder(),
    httpClient: HttpClient = mockk(relaxed = true),
    innboksVarselReader: InnboksVarselReader = mockk(relaxed = true),
    beskjedVarselReader: BeskjedVarselReader = mockk(relaxed = true),
    oppgaveVarselReader: OppgaveVarselReader = mockk(relaxed = true)
): Application.() -> Unit {
    return fun Application.() {
        api(
            authConfig = authConfig,
            httpClient = httpClient,
            innboksVarselReader = innboksVarselReader,
            oppgaveVarselReader = oppgaveVarselReader,
            beskjedVarselReader = beskjedVarselReader
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

fun mockClient(dummyContent: String) = HttpClient(
    MockEngine() {
        respond(
            content = dummyContent,
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, "application/json")
        )
    }
) {
    install(JsonFeature) {
        serializer = KotlinxSerializer()
    }
    install(HttpTimeout)
}

internal fun <T> T.createListFromObject(size: Int): List<T> = mutableListOf<T>().also { list ->
    for (i in 1..size) {
        list.add(this)
    }
}
