package no.nav.tms.event.api

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.tms.event.api.config.jsonConfig
import no.nav.tms.event.api.varsel.*
import org.amshove.kluent.internal.assertFalse
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApiTest {

    private val dummyFnr = "12345678910"

    @ParameterizedTest
    @ValueSource(strings = ["beskjed", "oppgave", "innboks"])
    fun `Henter aktive varsler fra tms-varsel-authority `(type: String) {
        val (aktiveMockresponse, aktiveExpectedResult) = mockContentLegacy(
            type = type,
            førstBehandlet = ZonedDateTime.now().minusDays(1),
            sistOppdatert = ZonedDateTime.now(),
            synligFremTil = ZonedDateTime.now().plusDays(10),
            size = 5,
        )
        testApplication {
            eventApiSetup {
                externalServices {
                    tmsAuthoritySetup("/detaljert/aktive", aktiveMockresponse, fnrHeaderShouldBe = dummyFnr)
                }
            }
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
    fun `Henter inaktive varsler fra tms-varsel-authority og gjør de om til DTO`(type: String) {
        val (inaktivMockresponse, inaktiveExpectedResult) = mockContentLegacy(
            type = type,
            førstBehandlet = ZonedDateTime.now().minusDays(1),
            sistOppdatert = ZonedDateTime.now(),
            synligFremTil = null,
            size = 2,
        )

        testApplication {
            eventApiSetup {
                externalServices {
                    tmsAuthoritySetup("/detaljert/inaktive", inaktivMockresponse, fnrHeaderShouldBe = dummyFnr)
                }
            }
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
    fun `Henter alle varsler fra tms-varsel-authority og gjør de om til DTO`(type: String) {
        val (alleMockresponse, alleExpectedResult) = mockContentLegacy(
            type = type,
            førstBehandlet = ZonedDateTime.now().minusDays(1),
            sistOppdatert = ZonedDateTime.now(),
            synligFremTil = ZonedDateTime.now().plusDays(3),
            size = 6,
        )

        testApplication {
            eventApiSetup {
                externalServices {
                    tmsAuthoritySetup("/detaljert/inaktive", alleMockresponse, fnrHeaderShouldBe = dummyFnr)
                }
            }
            client.get("/$type/inaktive") {
                header("fodselsnummer", "12345678910")
            }.apply {
                status shouldBeEqualTo HttpStatusCode.OK
                assertLegacyContent(bodyAsText(), alleExpectedResult)
            }
        }
    }
}

private val objectmapper = ObjectMapper()
private fun assertLegacyContent(content: String?, expectedResult: List<LegacyVarsel>) {
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

private fun assertContent(content: String?, expectedResult: List<DetaljertVarsel>) {
    val jsonObjects = objectmapper.readTree(content)
    jsonObjects.size() shouldBeEqualTo expectedResult.size
    val expectedObject = expectedResult.first()
    jsonObjects.first().also { resultObject ->
        resultObject["varselId"].textValue() shouldBeEqualTo expectedObject.varselId
        resultObject["produsent"]["namespace"].textValue() shouldBeEqualTo expectedObject.produsent.namespace
        resultObject["produsent"]["appnavn"].textValue() shouldBeEqualTo expectedObject.produsent.appnavn
        resultObject["sensitivitet"].textValue() shouldBeEqualTo expectedObject.sensitivitet.name
        resultObject["innhold"]["tekst"].textValue() shouldBeEqualTo expectedObject.innhold.tekst
        resultObject["innhold"]["link"].textValue() shouldBeEqualTo expectedObject.innhold.link
        resultObject["aktiv"].asBoolean() shouldBeEqualTo expectedObject.aktiv
        assertZonedDateTime(resultObject, expectedObject.aktivFremTil, "aktivFremTil")
        assertZonedDateTime(resultObject, expectedObject.opprettet, "opprettet")
        assertZonedDateTime(resultObject, expectedObject.inaktivert, "inaktivert")
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

private fun mockContentLegacy(
    type: String,
    førstBehandlet: ZonedDateTime,
    sistOppdatert: ZonedDateTime,
    synligFremTil: ZonedDateTime? = null,
    size: Int,
): Pair<String, List<LegacyVarsel>> {
    val synligFremTilString = synligFremTil?.let {
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
            "historikk": [
                { "status": "bestilt", "melding": "Varsel bestilt", "tidspunkt": "$sistOppdatert" },
                { "status": "sendt", "melding": "Varsel sendt på sms", "kanal": "SMS", "renotifikasjon": false, "tidspunkt": "$sistOppdatert" },
                { "status": "sendt", "melding": "Varsel sendt på epost", "kanal": "EPOST", "renotifikasjon": false, "tidspunkt": "$sistOppdatert" }
            ],
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
            eksternVarsling = LegacyEksternVarsling(
                sendt = true,
                renotifikasjonSendt = false,
                sendteKanaler = listOf("SMS", "EPOST"),
                prefererteKanaler = emptyList(),
                historikk = listOf(
                    LegacyEksternVarslingHistorikkEntry(
                        status = "bestilt",
                        melding = "Varsel bestilt",
                        tidspunkt = sistOppdatert,
                    ),
                    LegacyEksternVarslingHistorikkEntry(
                        status = "sendt",
                        melding = "Varsel sendt på sms",
                        kanal = "SMS",
                        renotifikasjon = false,
                        tidspunkt = sistOppdatert,
                    ),
                    LegacyEksternVarslingHistorikkEntry(
                        status = "sendt",
                        melding = "Varsel sendt på epost",
                        kanal = "EPOST",
                        renotifikasjon = false,
                        tidspunkt = sistOppdatert,
                    ),
                ),
            ),
        ) * size,
    )
}

private fun mockContent(
    opprettet: ZonedDateTime,
    inaktivert: ZonedDateTime? = null,
    aktivFremTil: ZonedDateTime? = null,
    vararg typer: String,
): List<Pair<String, DetaljertVarsel>> {
    return typer.map { type ->
        Pair(
            """{
            "type": "$type",
            "varselId": "abc-123",
            "opprettet": "${opprettet.withFixedOffsetZone()}",
            "produsent": { "namespace": "ns", "appnavn": "app" },
            "sensitivitet": "substantial",
            "inaktivert": ${inaktivert?.let { """"${it.withFixedOffsetZone()}"""" } ?: "null"},
            "aktivFremTil": ${aktivFremTil?.let { """"${it.withFixedOffsetZone()}"""" } ?: "null"},
            "innhold": { "tekst": "Tadda vi tester" },
            "aktiv": false,
            "eksternVarsling": {
                "sendt": true,
                "renotifikasjonSendt": true,
                "kanaler": ["SMS", "EPOST"],
                "historikk": [
                    { "status": "bestilt", "melding": "Varsel bestilt", "tidspunkt": "$opprettet" },
                    { "status": "sendt", "melding": "Varsel sendt på sms", "kanal": "SMS", "renotifikasjon": false, "tidspunkt": "$opprettet" },
                    { "status": "sendt", "melding": "Varsel sendt på epost", "kanal": "EPOST", "renotifikasjon": false, "tidspunkt": "$opprettet" }
                ],
                "sistOppdatert": "${opprettet.withFixedOffsetZone()}"
            }
          }""",
            DetaljertVarsel(
                type = type,
                varselId = "abc-123",
                opprettet = opprettet.withFixedOffsetZone(),
                produsent = Produsent("ns", "app"),
                sensitivitet = Sensitivitet.substantial,
                innhold = Innhold("Tadda vi tester"),
                aktiv = false,
                inaktivert = inaktivert?.withFixedOffsetZone(),
                aktivFremTil = aktivFremTil?.withFixedOffsetZone(),
                eksternVarsling = EksternVarslingStatus(
                    sendt = true,
                    renotifikasjonSendt = false,
                    kanaler = listOf("SMS", "EPOST"),
                    historikk = listOf(
                        EksternVarslingHistorikkEntry(
                            status = "bestilt",
                            melding = "Varsel bestilt",
                            tidspunkt = opprettet,
                        ),
                        EksternVarslingHistorikkEntry(
                            status = "sendt",
                            melding = "Varsel sendt på sms",
                            kanal = "SMS",
                            renotifikasjon = false,
                            tidspunkt = opprettet,
                        ),
                        EksternVarslingHistorikkEntry(
                            status = "sendt",
                            melding = "Varsel sendt på epost",
                            kanal = "EPOST",
                            renotifikasjon = false,
                            tidspunkt = opprettet,
                        ),
                    ),
                    sistOppdatert = opprettet,
                ),
            ),
        )
    }
}

private fun String.jsonArray(size: Int): String =
    (1..size).joinToString(separator = ",", prefix = "[", postfix = "]") { this }

private fun List<String>.jsonArray() = joinToString(separator = ",", prefix = "[", postfix = "]")

private fun TestApplication.applicationHttpClient() = createClient {
    jsonConfig()
}
