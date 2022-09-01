package no.nav.tms.event.api

import com.fasterxml.jackson.databind.JsonNode
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
import no.nav.tms.event.api.config.AzureTokenFetcher
import no.nav.tms.event.api.varsel.Varsel
import org.amshove.kluent.internal.assertFalse
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

private val objectmapper = ObjectMapper()

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApiTest {
    private val tokenFetchMock = mockk<AzureTokenFetcher>(relaxed = true)
    private val azureToken = "TokenSmoken"

    private val dummyFnr = "12345678910"

    @BeforeAll
    fun setup() {
        coEvery {
            tokenFetchMock.fetchTokenForEventHandler()
        } returns azureToken
    }

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

    @ParameterizedTest
    @ValueSource(strings = ["beskjed", "oppgave", "innboks"])
    fun `Henter aktive varsler fra eventhandler og gjør de om til DTO`(type: String) {
        val (aktiveMockresponse, aktiveExpectedResult) = mockContent(
            ZonedDateTime.now().minusDays(1),
            ZonedDateTime.now(),
            ZonedDateTime.now().plusDays(10),
            5
        )
        val mockClient = mockClient(
            aktiveMockresponse
        )

        withTestApplication(
            mockApi(
                httpClient = mockClient,
                azureTokenFetcher = tokenFetchMock
            )
        ) {
            assertVarselApiCall("/tms-event-api/$type/aktive", dummyFnr, aktiveExpectedResult)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["beskjed", "oppgave", "innboks"])
    fun `Henter inaktive varsler fra eventhandler og gjør de om til DTO`(type: String) {
        val (inaktivMockresponse, inaktiveExpectedResult) = mockContent(
            ZonedDateTime.now().minusDays(1),
            ZonedDateTime.now(),
            null,
            2
        )
        val mockClient = mockClient(
            inaktivMockresponse,
        )

        withTestApplication(
            mockApi(
                httpClient = mockClient,
                azureTokenFetcher = tokenFetchMock
            )
        ) {
            assertVarselApiCall("/tms-event-api/$type/inaktive", dummyFnr, inaktiveExpectedResult)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["beskjed", "oppgave", "innboks"])
    fun `Henter alle varsler fra eventhandler og gjør de om til DTO`(type: String) {
        val (alleMockresponse, alleExpectedResult) = mockContent(
            ZonedDateTime.now().minusDays(1),
            ZonedDateTime.now(),
            ZonedDateTime.now().plusDays(3),
            6
        )
        val mockClient = mockClient(
            alleMockresponse
        )

        withTestApplication(
            mockApi(
                httpClient = mockClient,
                azureTokenFetcher = tokenFetchMock
            )
        ) {
            assertVarselApiCall("/tms-event-api/$type/all", dummyFnr, alleExpectedResult)
        }
    }
}

private fun TestApplicationEngine.assertVarselApiCall(endpoint: String, fnr: String, expectedResult: List<Varsel>) {
    handleRequest(HttpMethod.Get, endpoint) {
        addHeader("fodselsnummer", fnr)
    }.also {
        it.response.status() shouldBeEqualTo HttpStatusCode.OK
        assertContent(it.response.content, expectedResult)
    }
}

private fun assertContent(content: String?, expectedResult: List<Varsel>) {
    val jsonObjects = objectmapper.readTree(content)
    jsonObjects.size() shouldBeEqualTo expectedResult.size
    val expectedObject = expectedResult.first()
    jsonObjects.first().also { resultObject ->
        resultObject["fodselsnummer"].textValue() shouldBeEqualTo expectedObject.fodselsnummer
        resultObject["grupperingsId"].textValue() shouldBeEqualTo expectedObject.grupperingsId
        resultObject["eventId"].textValue() shouldBeEqualTo expectedObject.eventId
        resultObject["produsent"].textValue() shouldBeEqualTo expectedObject.produsent
        resultObject["sikkerhetsnivaa"].asInt() shouldBeEqualTo expectedObject.sikkerhetsnivaa
        resultObject["tekst"].textValue() shouldBeEqualTo expectedObject.tekst
        resultObject["link"].textValue() shouldBeEqualTo expectedObject.link
        resultObject["aktiv"].asBoolean() shouldBeEqualTo expectedObject.aktiv
        assertZonedDateTime(resultObject, expectedObject.synligFremTil, "synligFremTil")
        assertZonedDateTime(resultObject, expectedObject.forstBehandlet, "forstBehandlet")
        assertZonedDateTime(resultObject, expectedObject.sistOppdatert, "sistOppdatert")
    }
}

private fun assertZonedDateTime(jsonNode: JsonNode?, expectedDate: ZonedDateTime?, key: String) {
    if (expectedDate != null) {
        val resultDate = ZonedDateTime.parse(jsonNode?.get(key)?.textValue()).truncatedTo(ChronoUnit.MINUTES)
        assertFalse(resultDate == null, "$key skal ikke være null")
        resultDate.toString() shouldBeEqualTo expectedDate.truncatedTo(ChronoUnit.MINUTES).toString()
    } else {
        jsonNode?.get(key)?.textValue() shouldBe null
    }
}

private fun allRoutes(root: Route): List<Route> {
    return listOf(root) + root.children.flatMap { allRoutes(it) }
        .filter { it.toString().contains("method") && it.toString() != "/" }
}

private fun mockContent(
    førstBehandlet: ZonedDateTime,
    sistOppdatert: ZonedDateTime,
    synligFremTil: ZonedDateTime? = null,
    size: Int
): Pair<String, List<Varsel>> {
    val synligFremTilString = synligFremTil?.let {
        """"${synligFremTil.withFixedOffsetZone()}""""
    } ?: "null"

    return Pair(
        """  {
        "fodselsnummer": "123",
        "grupperingsId": "",
        "eventId": "",
        "forstBehandlet": "${førstBehandlet.withFixedOffsetZone()}",
        "produsent": "",
        "sikkerhetsnivaa": 0,
        "sistOppdatert": "${sistOppdatert.withFixedOffsetZone()}",
        "synligFremTil": $synligFremTilString
        "tekst": "Tadda vi tester",
        "link": "",
        "aktiv": false
        "eksternVarslingSendt": true
        "eksternVarslingKanaler":["SMS", "EPOST"]
      }""".jsonArray(size),
        Varsel(
            fodselsnummer = "123",
            grupperingsId = "",
            eventId = "",
            forstBehandlet = førstBehandlet.withFixedOffsetZone(),
            produsent = "",
            sikkerhetsnivaa = 0,
            sistOppdatert = sistOppdatert.withFixedOffsetZone(),
            tekst = "Tadda vi tester",
            link = "",
            aktiv = false,
            synligFremTil = synligFremTil?.withFixedOffsetZone(),
            eksternVarslingSendt = true,
            eksternVarslingKanaler = listOf("SMS", "EPOST")
        ).createListFromObject(size)
    )
}

private fun String.jsonArray(size: Int): String =
    (1..size).joinToString(separator = ",", prefix = "[", postfix = "]") { this }
