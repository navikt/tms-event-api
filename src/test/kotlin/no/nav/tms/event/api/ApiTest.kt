package no.nav.tms.event.api

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
import no.nav.tms.event.api.beskjed.BeskjedEventService
import no.nav.tms.event.api.common.User
import no.nav.tms.event.api.innboks.InnboksDTO
import no.nav.tms.event.api.innboks.InnboksEventService
import no.nav.tms.event.api.oppgave.OppgaveDTO
import no.nav.tms.event.api.oppgave.OppgaveEventService
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.ZonedDateTime

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
        val beskjedEventService = mockk<BeskjedEventService>()
        val rootPath = "/tms-event-api/beskjed"
        coEvery { beskjedEventService.getInactiveCachedEventsForUser(User(dummyFnr)) } returns dummyBeskjeder(5)
        coEvery { beskjedEventService.getActiveCachedEventsForUser(User(dummyFnr)) } returns dummyBeskjeder(1)
        coEvery { beskjedEventService.getAllCachedEventsForUser(User(dummyFnr)) } returns dummyBeskjeder(6)

        withTestApplication(mockApi(beskjedEventService = beskjedEventService)) {
            assertVarselApiCall("$rootPath/inaktive", dummyFnr, 5)
            assertVarselApiCall("$rootPath/aktive", dummyFnr, 1)
            assertVarselApiCall("$rootPath/all", dummyFnr, 6)
        }
    }

    @Test
    fun oppgavevarsler() {
        val dummyFnr = "16045571871"
        val oppgaveEventService = mockk<OppgaveEventService>()
        val rootPath = "/tms-event-api/oppgave"
        coEvery { oppgaveEventService.getInactiveCachedEventsForUser(User(dummyFnr)) } returns dummyOppgaver(5)
        coEvery { oppgaveEventService.getActiveCachedEventsForUser(User(dummyFnr)) } returns dummyOppgaver(1)
        coEvery { oppgaveEventService.getAllCachedEventsForUser(User(dummyFnr)) } returns dummyOppgaver(6)

        withTestApplication(mockApi(oppgaveEventService = oppgaveEventService)) {
            assertVarselApiCall("$rootPath/inaktive", dummyFnr, 5)
            assertVarselApiCall("$rootPath/aktive", dummyFnr, 1)
            assertVarselApiCall("$rootPath/all", dummyFnr, 6)
        }
    }

    @Test
    fun innboksvarsler() {
        val dummyFnr = "16045571871"
        val innboksEventService = mockk<InnboksEventService>()
        val rootPath = "/tms-event-api/innboks"
        coEvery { innboksEventService.getInactiveCachedEventsForUser(User(dummyFnr)) } returns dummyInnboks(3)
        coEvery { innboksEventService.getActiveCachedEventsForUser(User(dummyFnr)) } returns dummyInnboks(1)
        coEvery { innboksEventService.getAllCachedEventsForUser(User(dummyFnr)) } returns dummyInnboks(6)

        withTestApplication(mockApi(innboksEventService = innboksEventService)) {
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

private fun dummyBeskjeder(antall: Int = 0): List<BeskjedDTO> = BeskjedDTO(
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
).createList(antall = antall)

private fun dummyOppgaver(antall: Int = 0): List<OppgaveDTO> = OppgaveDTO(
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
).createList(antall)

private fun dummyInnboks(antall: Int): List<InnboksDTO> = InnboksDTO(
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
).createList(antall)

private fun InnboksDTO.createList(antall: Int): List<InnboksDTO> = mutableListOf<InnboksDTO>().also { list ->
    for (i in 1..antall) {
        list.add(this)
    }
}

private fun OppgaveDTO.createList(antall:Int): List<OppgaveDTO> = mutableListOf<OppgaveDTO>() .also { list ->
    for (i in 1..antall) {
        list.add(this)
    }
}

private fun BeskjedDTO.createList(antall: Int): MutableList<BeskjedDTO> =
    mutableListOf<BeskjedDTO>().also { list ->
        for (i in 1..antall) {
            list.add(this)
        }
    }


fun allRoutes(root: Route): List<Route> {
    return listOf(root) + root.children.flatMap { allRoutes(it) }
        .filter { it.toString().contains("method") && it.toString() != "/" }
}
