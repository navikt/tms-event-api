package no.nav.tms.event.api

import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.tms.event.api.varsel.*
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

            var fnrHeader: String? = null

            setupExternalVarselRoute(
                testHostUrl,
                path = "$type/detaljert/aktive",
                responseBody = aktiveMockresponse,
                requestPeek = {
                    fnrHeader = it.headers["ident"]
                },
            )

            client.get("/$type/aktive") {
                header("fodselsnummer", dummyFnr)
            }.apply {
                status shouldBe HttpStatusCode.OK
                assertLegacyContent(bodyAsText(), aktiveExpectedResult)
            }

            fnrHeader shouldBe dummyFnr
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

            var fnrHeader: String? = null

            setupExternalVarselRoute(
                host = testHostUrl,
                path = "/$type/detaljert/inaktive",
                responseBody = inaktivMockresponse,
                requestPeek = {
                    fnrHeader = it.headers["ident"]
                },
            )
            client.get("/$type/inaktive") {
                header("fodselsnummer", dummyFnr)
            }.apply {
                status shouldBe HttpStatusCode.OK
                assertLegacyContent(bodyAsText(), inaktiveExpectedResult)
            }

            fnrHeader shouldBe dummyFnr
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

            var fnrHeader: String? = null

            setupExternalVarselRoute(
                host = testHostUrl,
                path = "/$type/detaljert/alle",
                responseBody = alleMockresponse,
                requestPeek = {
                    fnrHeader = it.headers["ident"]
                },
            )

            client.get("/$type/all") {
                header("fodselsnummer", dummyFnr)
            }.apply {
                status shouldBe HttpStatusCode.OK
                assertLegacyContent(bodyAsText(), alleExpectedResult)
            }

            fnrHeader shouldBe dummyFnr
        }
    }
}

private fun assertLegacyContent(
    content: String?,
    expectedResult: List<LegacyVarsel>,
) {
    val jsonObjects = objectmapper.readTree(content)
    jsonObjects.size() shouldBe expectedResult.size
    val expectedObject = expectedResult.first()
    jsonObjects.first().also { resultObject ->
        resultObject["fodselsnummer"].textValue() shouldBe expectedObject.fodselsnummer
        resultObject["grupperingsId"].textValue() shouldBe expectedObject.grupperingsId
        resultObject["eventId"].textValue() shouldBe expectedObject.eventId
        resultObject["produsent"].textValue() shouldBe expectedObject.produsent
        resultObject["sikkerhetsnivaa"].asInt() shouldBe expectedObject.sikkerhetsnivaa
        resultObject["tekst"].textValue() shouldBe expectedObject.tekst
        resultObject["link"].textValue() shouldBe expectedObject.link
        resultObject["aktiv"].asBoolean() shouldBe expectedObject.aktiv
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
