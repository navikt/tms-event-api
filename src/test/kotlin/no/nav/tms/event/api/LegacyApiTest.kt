package no.nav.tms.event.api

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.tms.common.testutils.initExternalServices
import no.nav.tms.event.api.varsel.*
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.ZonedDateTime

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LegacyApiTest {
    private val dummyFnr = "12345678910"
    private val testHostUrl = "https://www.test.no"

    @ParameterizedTest
    @ValueSource(strings = ["beskjed", "oppgave", "innboks"])
    fun `Henter aktive varsler på legacy format fra tms-varsel-authority `(type: String) {
        val (aktiveMockresponse, aktiveExpectedResult) =
            mockContentAndExpectedLegacyResponse(
                type = type,
                førstBehandlet = ZonedDateTime.now().minusDays(1),
                sistOppdatert = ZonedDateTime.now(),
                synligFremTil = ZonedDateTime.now().plusDays(10),
                size = 5,
            )
        testApplication {
            eventApiSetup(testHostUrl)
            initExternalServices(
                testHostUrl,
                VarselRouteProvider(
                    type = type,
                    endpoint = "/detaljert/aktive",
                    fnrHeaderShouldBe = dummyFnr,
                    responseBody = aktiveMockresponse,
                ),
            )

            client.get("/$type/aktive") {
                header("fodselsnummer", "12345678910")
            }.apply {
                status shouldBeEqualTo HttpStatusCode.OK
                assertLegacyContent(bodyAsText(), aktiveExpectedResult)
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["beskjed", "oppgave", "innboks"])
    fun `Henter inaktive varsler på legacy format fra tms-varsel-authority og gjør de om til DTO`(type: String) {
        val (inaktivMockresponse, inaktiveExpectedResult) =
            mockContentAndExpectedLegacyResponse(
                type = type,
                førstBehandlet = ZonedDateTime.now().minusDays(1),
                sistOppdatert = ZonedDateTime.now(),
                synligFremTil = null,
                size = 2,
            )

        testApplication {
            eventApiSetup(testHostUrl)
            initExternalServices(
                testHostUrl,
                VarselRouteProvider(
                    type = type,
                    endpoint = "/detaljert/inaktive",
                    fnrHeaderShouldBe = dummyFnr,
                    responseBody = inaktivMockresponse,
                ),
            )
            client.get("/$type/inaktive") {
                header("fodselsnummer", "12345678910")
            }.apply {
                status shouldBeEqualTo HttpStatusCode.OK
                assertLegacyContent(bodyAsText(), inaktiveExpectedResult)
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["beskjed", "oppgave", "innboks"])
    fun `Henter alle varsler på legacy format fra tms-varsel-authority og gjør de om til DTO`(type: String) {
        val (alleMockresponse, alleExpectedResult) =
            mockContentAndExpectedLegacyResponse(
                type = type,
                førstBehandlet = ZonedDateTime.now().minusDays(1),
                sistOppdatert = ZonedDateTime.now(),
                synligFremTil = ZonedDateTime.now().plusDays(3),
                size = 6,
            )

        testApplication {
            eventApiSetup(testHostUrl)
            initExternalServices(
                testHostUrl,
                VarselRouteProvider(
                    type = type,
                    endpoint = "/detaljert/alle",
                    fnrHeaderShouldBe = dummyFnr,
                    responseBody = alleMockresponse,
                ),
            )
            client.get("/$type/all") {
                header("fodselsnummer", "12345678910")
            }.apply {
                status shouldBeEqualTo HttpStatusCode.OK
                assertLegacyContent(bodyAsText(), alleExpectedResult)
            }
        }
    }
}

private fun assertLegacyContent(
    content: String?,
    expectedResult: List<LegacyVarsel>,
) {
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

private fun mockContentAndExpectedLegacyResponse(
    type: String,
    førstBehandlet: ZonedDateTime,
    sistOppdatert: ZonedDateTime,
    synligFremTil: ZonedDateTime? = null,
    size: Int,
): Pair<String, List<LegacyVarsel>> {
    val synligFremTilString =
        synligFremTil?.let {
            """"${synligFremTil.withFixedOffsetZone()}""""
        } ?: "null"

    return Pair(
        """{
        "type": "$type",
        "varselId": "abc-123",
        "opprettet": "${førstBehandlet.withFixedOffsetZone()}",
        "produsent": { "namespace": "ns", "appnavn": "app" },
        "sensitivitet": "substantial",
        "inaktivert": "${sistOppdatert.withFixedOffsetZone()}",
        "aktivFremTil": $synligFremTilString,
        "innhold": { "tekst": "Tadda vi tester" },
        "aktiv": false,
        "eksternVarsling": {
            "sendt": true,
            "renotifikasjonSendt": true,
            "kanaler": ["SMS", "EPOST"],
            "sistOppdatert": "${sistOppdatert.withFixedOffsetZone()}"
        }
      }""".jsonArray(size),
        LegacyVarsel(
            fodselsnummer = "",
            grupperingsId = "",
            eventId = "abc-123",
            forstBehandlet = førstBehandlet.withFixedOffsetZone(),
            produsent = "app",
            sikkerhetsnivaa = 3,
            sistOppdatert = sistOppdatert.withFixedOffsetZone(),
            tekst = "Tadda vi tester",
            link = "",
            aktiv = false,
            synligFremTil = synligFremTil?.withFixedOffsetZone(),
            eksternVarsling =
                LegacyEksternVarsling(
                    sendt = true,
                    renotifikasjonSendt = false,
                    sendteKanaler = listOf("SMS", "EPOST"),
                    prefererteKanaler = emptyList()
                ),
        ) * size,
    )
}

private fun String.jsonArray(size: Int): String = (1..size).joinToString(separator = ",", prefix = "[", postfix = "]") { this }
