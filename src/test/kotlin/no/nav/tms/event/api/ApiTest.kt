package no.nav.tms.event.api

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import nav.no.tms.common.testutils.initExternalServices
import no.nav.tms.event.api.varsel.*
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

class ApiTest {
    private val dummyFnr = "12345678910"
    private val testHostUrl = "https://www.test.no"

    @Test
    fun `Henter aktive varsler fra tms-varsel-authority `() {
        val (responseBody, expectedContent) =
            mockContent(
                opprettet = ZonedDateTime.now().minusDays(1),
                aktivFremTil = ZonedDateTime.now().plusDays(10),
                typer = listOf("beskjed", "oppgave"),
            )
        testApplication {
            eventApiSetup(testHostUrl)
            initExternalServices(
                testHostUrl,
                VarselRouteProvider(
                    type = "varsel",
                    endpoint = "detaljert/aktive",
                    fnrHeaderShouldBe = dummyFnr,
                    responseBody = responseBody,
                ),
            )

            client.get("/varsel/aktive") {
                header("fodselsnummer", "12345678910")
            }.apply {
                status shouldBeEqualTo HttpStatusCode.OK
                assertContent(bodyAsText(), expectedContent)
            }
        }
    }

    @Test
    fun `Henter inaktive varsler fra tms-varsel-authority og gjør de om til DTO`() {
        val (inaktivMockresponse, inaktiveExpectedResult) =
            mockContent(
                opprettet = ZonedDateTime.now().minusDays(1),
                inaktivert = ZonedDateTime.now(),
                typer = listOf("beskjed", "oppgave"),
            )

        testApplication {
            eventApiSetup(testHostUrl)
            initExternalServices(
                testHostUrl,
                VarselRouteProvider(
                    type = "varsel",
                    endpoint = "/detaljert/inaktive",
                    fnrHeaderShouldBe = dummyFnr,
                    responseBody = inaktivMockresponse,
                ),
            )
            client.get("/varsel/inaktive") {
                header("fodselsnummer", "12345678910")
            }.apply {
                status shouldBeEqualTo HttpStatusCode.OK
                assertContent(bodyAsText(), inaktiveExpectedResult)
            }
        }
    }

    @Test
    fun `Henter alle varsler fra tms-varsel-authority og gjør de om til DTO`() {
        val (alleMockresponse, alleExpectedResult) =
            mockContent(
                opprettet = ZonedDateTime.now().minusDays(1),
                aktivFremTil = ZonedDateTime.now().plusDays(10),
                typer = listOf("beskjed", "oppgave"),
            )

        testApplication {
            eventApiSetup(testHostUrl)
            initExternalServices(
                testHostUrl,
                VarselRouteProvider(
                    type = "varsel",
                    endpoint = "/detaljert/alle",
                    fnrHeaderShouldBe = dummyFnr,
                    responseBody = alleMockresponse,
                ),
            )
            client.get("/varsel/alle") {
                header("fodselsnummer", "12345678910")
            }.apply {
                status shouldBeEqualTo HttpStatusCode.OK
                assertContent(bodyAsText(), alleExpectedResult)
            }
        }
    }
}

private fun List<Pair<String, DetaljertVarsel>>.mapExternalServiceResponse(): List<String> = map { it.first }

private fun mockContent(
    opprettet: ZonedDateTime,
    inaktivert: ZonedDateTime? = null,
    aktivFremTil: ZonedDateTime? = null,
    typer: List<String>,
): Pair<String, List<DetaljertVarsel>> {
    return typer
        .map { type ->
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
                    eksternVarsling =
                        EksternVarslingStatus(
                            sendt = true,
                            renotifikasjonSendt = false,
                            kanaler = listOf("SMS", "EPOST"),
                            historikk =
                                listOf(
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
        .let { generatedContent ->
            Pair(
                generatedContent.joinToString(separator = ",", prefix = "[", postfix = "]") { it.first },
                generatedContent.map { it.second },
            )
        }
}

fun assertContent(
    content: String?,
    expectedResult: List<DetaljertVarsel>,
) {
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
