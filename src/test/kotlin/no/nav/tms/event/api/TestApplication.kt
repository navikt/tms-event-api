package no.nav.tms.event.api

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.application.Application
import io.ktor.client.HttpClient
import io.mockk.mockk
import no.nav.tms.event.api.beskjed.BeskjedEventService
import no.nav.tms.event.api.config.api
import no.nav.tms.event.api.health.HealthService
import no.nav.tms.event.api.innboks.InnboksEventService
import no.nav.tms.event.api.oppgave.OppgaveReader
import no.nav.tms.token.support.authentication.installer.mock.installMockedAuthenticators
import no.nav.tms.token.support.tokenx.validation.mock.SecurityLevel

val objectmapper = ObjectMapper()
fun mockApi(
    authConfig: Application.() -> Unit = mockAuthBuilder(),
    httpClient: HttpClient = mockk(relaxed = true),
    innboksEventService: InnboksEventService = mockk(relaxed = true),
    beskjedEventService: BeskjedEventService = mockk(relaxed = true),
    oppgaveReader: OppgaveReader = mockk(relaxed = true),
    healthService: HealthService = mockk(relaxed = true)
): Application.() -> Unit {
    return fun Application.() {
        api(
            authConfig = authConfig,
            httpClient = httpClient,
            innboksEventService = innboksEventService,
            oppgaveReader = oppgaveReader,
            beskjedEventService = beskjedEventService, healthService = healthService
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
