package no.nav.tms.event.api

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.application.feature
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.routing.Route
import io.ktor.routing.Routing
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nav.tms.event.api.config.AzureToken
import no.nav.tms.event.api.config.AzureTokenFetcher
import no.nav.tms.event.api.varsel.Varsel
import no.nav.tms.event.api.varsel.VarselDTO
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.ZonedDateTime

private val objectmapper = ObjectMapper()

class ApiTest {
    private val tokenFetchMock = mockk<AzureTokenFetcher>(relaxed = true)
    private val azureToken = AzureToken("TokenSmoken")

    @Test
    fun `setter opp api ruter`() {
        withTestApplication(
            mockApi(
                httpClient = mockClient(""),
                azureTokenFetcher = tokenFetchMock
            )
        ) {
            allRoutes(this.application.feature(Routing)).size shouldBeEqualTo 13
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["beskjed", "oppgave", "innboks"])
    fun `bad request for ugyldig fødselsnummer i header`(varselType: String) {
        withTestApplication(
            mockApi(
                httpClient = mockClient(""),
                azureTokenFetcher = tokenFetchMock
            )
        ) {
            handleRequest {
                handleRequest(HttpMethod.Get, "/tms-event-api/$varselType/aktive").also {
                    it.response.status() shouldBeEqualTo HttpStatusCode.BadRequest
                    it.response.content shouldBeEqualTo "Requesten mangler header-en 'fodselsnummer'"
                }
                handleRequest(HttpMethod.Get, "/tms-event-api/$varselType/inaktive") {
                    addHeader("fodselsnummer", "1234")
                }.also {
                    it.response.status() shouldBeEqualTo HttpStatusCode.BadRequest
                    it.response.content shouldBeEqualTo "Header-en 'fodselsnummer' inneholder ikke et gyldig fødselsnummer."
                }
            }
        }
    }

    @Test
    fun beskjedvarsler() {
        val dummyFnr = "12345678910"
        val rootPath = "/tms-event-api/beskjed"

        val (mockresponse, expectedResult) = mockContent(
            ZonedDateTime.now().minusDays(1),
            ZonedDateTime.now()
        )

        coEvery {
            tokenFetchMock.fetchTokenForEventHandler()
        } returns azureToken

        withTestApplication(
            mockApi(
                httpClient =mockClient(Json.encodeToString(mockresponse)),
                azureTokenFetcher = tokenFetchMock
            )
        ) {
            assertVarselApiCall("$rootPath/inaktive", dummyFnr, 5)
            assertVarselApiCall("$rootPath/aktive", dummyFnr, 1)
            assertVarselApiCall("$rootPath/all", dummyFnr, 6)
        }
    }
    /*
        @Test
        fun oppgavevarsler() {
            val dummyFnr = "16045571871"
            val oppgaveVarselReader = mockk<OppgaveVarselReader>()
            val rootPath = "/tms-event-api/oppgave"
            coEvery { oppgaveVarselReader.inaktiveVarsler(dummyFnr) } returns dummyVarsel(5)
            coEvery { oppgaveVarselReader.aktiveVarsler(dummyFnr) } returns dummyVarsel(1)
            coEvery { oppgaveVarselReader.alleVarsler(dummyFnr) } returns dummyVarsel(6)

            withTestApplication(mockApi(oppgaveVarselReader = oppgaveVarselReader)) {
                assertVarselApiCall("$rootPath/inaktive", dummyFnr, 5)
                assertVarselApiCall("$rootPath/aktive", dummyFnr, 1)
                assertVarselApiCall("$rootPath/all", dummyFnr, 6)
            }
        }

        @Test
        fun innboksvarsler() {
            val dummyFnr = "16045571871"
            val varselReader = mockk<VarselReader>()
            val rootPath = "/tms-event-api/innboks"
          //  coEvery { varselReader.inaktiveVarsler(dummyFnr) } returns dummyVarsel(3)
          //  coEvery { varselReader.aktiveVarsler(dummyFnr) } returns dummyVarsel(1)
          //  coEvery { varselReader.alleVarsler(dummyFnr) } returns dummyVarsel(6)

            withTestApplication(mockApi(varselReader = varselReader)) {
                assertVarselApiCall("$rootPath/inaktive", dummyFnr, 3)
                assertVarselApiCall("$rootPath/aktive", dummyFnr, 1)
                assertVarselApiCall("$rootPath/all", dummyFnr, 6)
            }
        }*/
}

private fun TestApplicationEngine.assertVarselApiCall(endpoint: String, fnr: String, expectedSize: Int) {
    handleRequest(HttpMethod.Get, endpoint) {
        addHeader("fodselsnummer", fnr)
    }.also {
        println(it.response.content)
        it.response.status() shouldBeEqualTo HttpStatusCode.OK
        objectmapper.readTree(it.response.content).size() shouldBeEqualTo expectedSize
    }
}

fun allRoutes(root: Route): List<Route> {
    return listOf(root) + root.children.flatMap { allRoutes(it) }
        .filter { it.toString().contains("method") && it.toString() != "/" }
}

private fun mockContent(
    førstBehandlet: ZonedDateTime,
    sistOppdatert: ZonedDateTime
): Pair<List<Varsel>, List<VarselDTO>> {
    return Pair(
        Varsel(
            fodselsnummer = "123",
            grupperingsId = "",
            eventId = "",
            forstBehandlet = førstBehandlet,
            produsent = "",
            sikkerhetsnivaa = 0,
            sistOppdatert = sistOppdatert,
            tekst = "Tadda vi tester",
            link = "",
            aktiv = false,
            eksternVarslingSendt = false,
            eksternVarslingKanaler = listOf(),
            synligFremTil = sistOppdatert

        ).createListFromObject(5),
        VarselDTO(
            fodselsnummer = "123",
            grupperingsId = "",
            eventId = "",
            forstBehandlet = førstBehandlet.withFixedOffsetZone(),
            produsent = "",
            sikkerhetsnivaa = 0,
            sistOppdatert = sistOppdatert.withFixedOffsetZone(),
            tekst = "Tadda vi tester",
            link = "",
            aktiv = false
        ).createListFromObject(5)

    )
}
