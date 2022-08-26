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
import no.nav.tms.event.api.beskjed.BeskjedDTO
import no.nav.tms.event.api.beskjed.BeskjedVarselReader
import no.nav.tms.event.api.innboks.InnboksDTO
import no.nav.tms.event.api.innboks.InnboksVarselReader
import no.nav.tms.event.api.oppgave.OppgaveDTO
import no.nav.tms.event.api.oppgave.OppgaveVarselReader
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.ZonedDateTime

private val objectmapper = ObjectMapper()
class ApiTest {
    @Test
    fun `setter opp api ruter`() {
        withTestApplication(mockApi()) {
            allRoutes(this.application.feature(Routing)).size shouldBeEqualTo 14
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["beskjed", "oppgave", "innboks"])
    fun `bad request for ugyldig fødselsnummer i header`(varselType: String) {
        withTestApplication(mockApi()) {
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
        val dummyFnr = "16045571871"
        val beskjedVarselReader = mockk<BeskjedVarselReader>()
        val rootPath = "/tms-event-api/beskjed"
        coEvery { beskjedVarselReader.inaktiveVarsler(dummyFnr) } returns dummyBeskjeder(5)
        coEvery { beskjedVarselReader.aktiveVarsler(dummyFnr) } returns dummyBeskjeder(1)
        coEvery { beskjedVarselReader.alleVarsler(dummyFnr) } returns dummyBeskjeder(6)

        withTestApplication(mockApi(beskjedVarselReader = beskjedVarselReader)) {
            assertVarselApiCall("$rootPath/inaktive", dummyFnr, 5)
            assertVarselApiCall("$rootPath/aktive", dummyFnr, 1)
            assertVarselApiCall("$rootPath/all", dummyFnr, 6)
        }
    }

    @Test
    fun oppgavevarsler() {
        val dummyFnr = "16045571871"
        val oppgaveVarselReader = mockk<OppgaveVarselReader>()
        val rootPath = "/tms-event-api/oppgave"
        coEvery { oppgaveVarselReader.inaktiveVarsler(dummyFnr) } returns dummyOppgaveDTOs(5)
        coEvery { oppgaveVarselReader.aktiveVarsler(dummyFnr) } returns dummyOppgaveDTOs(1)
        coEvery { oppgaveVarselReader.alleVarsler(dummyFnr) } returns dummyOppgaveDTOs(6)

        withTestApplication(mockApi(oppgaveVarselReader = oppgaveVarselReader)) {
            assertVarselApiCall("$rootPath/inaktive", dummyFnr, 5)
            assertVarselApiCall("$rootPath/aktive", dummyFnr, 1)
            assertVarselApiCall("$rootPath/all", dummyFnr, 6)
        }
    }

    @Test
    fun innboksvarsler() {
        val dummyFnr = "16045571871"
        val innboksVarselReader = mockk<InnboksVarselReader>()
        val rootPath = "/tms-event-api/innboks"
        coEvery { innboksVarselReader.inaktiveVarsler(dummyFnr) } returns dummyInnboks(3)
        coEvery { innboksVarselReader.aktiveVarsler(dummyFnr) } returns dummyInnboks(1)
        coEvery { innboksVarselReader.alleVarsler(dummyFnr) } returns dummyInnboks(6)

        withTestApplication(mockApi(innboksVarselReader = innboksVarselReader)) {
            assertVarselApiCall("$rootPath/inaktive", dummyFnr, 3)
            assertVarselApiCall("$rootPath/aktive", dummyFnr, 1)
            assertVarselApiCall("$rootPath/all", dummyFnr, 6)
        }
    }
}

private fun TestApplicationEngine.assertVarselApiCall(endpoint: String, fnr: String, expectedSize: Int) {
    handleRequest(HttpMethod.Get, endpoint) {
        addHeader("fodselsnummer", fnr)
    }.also {
        it.response.status() shouldBeEqualTo HttpStatusCode.OK
        objectmapper.readTree(it.response.content).size() shouldBeEqualTo expectedSize
    }
}

fun allRoutes(root: Route): List<Route> {
    return listOf(root) + root.children.flatMap { allRoutes(it) }
        .filter { it.toString().contains("method") && it.toString() != "/" }
}

private fun dummyBeskjeder(size: Int = 0): List<BeskjedDTO> = BeskjedDTO(
    fodselsnummer = "",
    grupperingsId = "",
    eventId = "",
    forstBehandlet = ZonedDateTime.now().minusMinutes(9),
    produsent = "",
    sikkerhetsnivaa = 0,
    sistOppdatert = ZonedDateTime.now(),
    synligFremTil = ZonedDateTime.now().plusDays(1),
    tekst = "",
    link = "",
    aktiv = false
).createListFromObject(size = size)
private fun dummyOppgaveDTOs(size: Int = 0): List<OppgaveDTO> = OppgaveDTO(
    fodselsnummer = "",
    grupperingsId = "",
    eventId = "",
    forstBehandlet = ZonedDateTime.now().minusMinutes(9),
    produsent = "",
    sikkerhetsnivaa = 0,
    sistOppdatert = ZonedDateTime.now(),
    tekst = "",
    link = "",
    aktiv = false
).createListFromObject(size)
private fun dummyInnboks(size: Int): List<InnboksDTO> = InnboksDTO(
    produsent = "",
    forstBehandlet = ZonedDateTime.now().minusMinutes(9),
    fodselsnummer = "",
    eventId = "",
    grupperingsId = "",
    tekst = "",
    link = "",
    sikkerhetsnivaa = 0,
    sistOppdatert = ZonedDateTime.now(),
    aktiv = false
).createListFromObject(size)
