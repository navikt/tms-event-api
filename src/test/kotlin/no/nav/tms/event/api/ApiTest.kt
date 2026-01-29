package no.nav.tms.event.api

import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.tms.event.api.varsel.*
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
                typer = listOf("beskjed", "oppgave", "innboks"),
            )
        testApplication {
            eventApiSetup(testHostUrl)

            var fnrHeader: String? = null

            setupExternalVarselRoute(
                host = testHostUrl,
                path = "/varsel/detaljert/aktive",
                responseBody = responseBody,
                requestPeek = {
                    fnrHeader = it.headers["ident"]
                },
            )

            client.get("/varsel/aktive") {
                header("fodselsnummer", dummyFnr)
            }.apply {
                status shouldBe HttpStatusCode.OK
                assertContent(bodyAsText(), expectedContent)
            }

            fnrHeader shouldBe dummyFnr
        }
    }

    @Test
    fun `Henter inaktive varsler fra tms-varsel-authority og gjør de om til DTO`() {
        val (inaktivMockresponse, inaktiveExpectedResult) =
            mockContent(
                opprettet = ZonedDateTime.now().minusDays(1),
                inaktivert = ZonedDateTime.now(),
                typer = listOf("beskjed", "oppgave", "innboks"),
            )

        testApplication {
            eventApiSetup(testHostUrl)

            var fnrHeader: String? = null

            setupExternalVarselRoute(
                host = testHostUrl,
                path = "/varsel/detaljert/inaktive",
                responseBody = inaktivMockresponse,
                requestPeek = {
                    fnrHeader = it.headers["ident"]
                },
            )

            client.get("/varsel/inaktive") {
                header("fodselsnummer", "12345678910")
            }.apply {
                status shouldBe HttpStatusCode.OK
                assertContent(bodyAsText(), inaktiveExpectedResult)
            }

            fnrHeader shouldBe dummyFnr
        }
    }

    @Test
    fun `Henter alle varsler fra tms-varsel-authority og gjør de om til DTO`() {
        val (alleMockresponse, alleExpectedResult) =
            mockContent(
                opprettet = ZonedDateTime.now().minusDays(1),
                aktivFremTil = ZonedDateTime.now().plusDays(10),
                typer = listOf("beskjed", "oppgave", "innboks"),
            )

        testApplication {
            eventApiSetup(testHostUrl)

            var fnrHeader: String? = null

            setupExternalVarselRoute(
                host = testHostUrl,
                path = "varsel/detaljert/alle",
                responseBody = alleMockresponse,
                requestPeek = {
                    fnrHeader = it.headers["ident"]
                }
            )

            client.get("/varsel/alle") {
                header("fodselsnummer", "12345678910")
            }.apply {
                status shouldBe HttpStatusCode.OK
                assertContent(bodyAsText(), alleExpectedResult)
            }

            fnrHeader shouldBe dummyFnr
        }
    }

    @Test
    fun `Overleverer info om tidspunkt for ekstern varsling`() {
        val (alleMockresponse, alleExpectedResult) =
            mockContent(
                opprettet = ZonedDateTime.now().minusDays(1),
                aktivFremTil = ZonedDateTime.now().plusDays(10),
                sendtTidspunkt = ZonedDateTime.now().minusHours(12),
                renotifikasjonTidspunkt = ZonedDateTime.now().plusDays(3),
                typer = listOf("beskjed", "oppgave", "innboks"),
            )

        testApplication {
            eventApiSetup(testHostUrl)

            var fnrHeader: String? = null

            setupExternalVarselRoute(
                host = testHostUrl,
                path = "varsel/detaljert/alle",
                responseBody = alleMockresponse,
                requestPeek = {
                    fnrHeader = it.headers["ident"]
                }
            )

            client.get("/varsel/alle") {
                header("fodselsnummer", "12345678910")
            }.apply {
                status shouldBe HttpStatusCode.OK
                assertContent(bodyAsText(), alleExpectedResult)
            }

            fnrHeader shouldBe dummyFnr
        }
    }
}

private fun mockContent(
    opprettet: ZonedDateTime,
    inaktivert: ZonedDateTime? = null,
    aktivFremTil: ZonedDateTime? = null,
    sendtTidspunkt: ZonedDateTime? = null,
    renotifikasjonTidspunkt: ZonedDateTime? = null,
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
            "inaktivert": ${inaktivert?.withFixedOffsetZone().nullableJson()},
            "aktivFremTil": ${aktivFremTil?.withFixedOffsetZone().nullableJson()},
            "innhold": { "tekst": "Tadda vi tester" },
            "aktiv": false,
            "eksternVarsling": {
                "sendt": true,
                "sendtTidspunkt": ${sendtTidspunkt?.withFixedOffsetZone().nullableJson()},
                "sendtSomBatch": false,
                "renotifikasjonSendt": true,
                "renotifikasjonTidspunkt": ${renotifikasjonTidspunkt?.withFixedOffsetZone().nullableJson()},
                "kanaler": ["SMS"],
                "feilhistorikk": [{ "feilmelding": "Kortvarig feil", "tidspunkt": "${opprettet.withFixedOffsetZone()}" }],
                "sistOppdatert": "${opprettet.plusDays(7).withFixedOffsetZone()}"
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
                            sendtSomBatch = false,
                            sendtTidspunkt = sendtTidspunkt?.withFixedOffsetZone(),
                            renotifikasjonSendt = true,
                            renotifikasjonTidspunkt = renotifikasjonTidspunkt?.withFixedOffsetZone(),
                            kanaler = listOf("SMS"),
                            feilhistorikk = listOf(
                                EksternFeilHistorikkEntry("Kortvarig feil", opprettet.withFixedOffsetZone())
                            ),
                            sistOppdatert = opprettet.plusDays(7).withFixedOffsetZone(),
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

private fun Any?.nullableJson(): String {
    return if (this == null) {
        "null"
    } else {
        "\"$this\""
    }
}

fun assertContent(
    content: String?,
    expectedResult: List<DetaljertVarsel>,
) {
    val jsonObjects = objectmapper.readTree(content)
    jsonObjects.size() shouldBe expectedResult.size
    val expectedObject = expectedResult.first()
    jsonObjects.first().also { resultObject ->
        resultObject["varselId"].textValue() shouldBe expectedObject.varselId
        resultObject["produsent"]["namespace"].textValue() shouldBe expectedObject.produsent.namespace
        resultObject["produsent"]["appnavn"].textValue() shouldBe expectedObject.produsent.appnavn
        resultObject["sensitivitet"].textValue() shouldBe expectedObject.sensitivitet.name
        resultObject["innhold"]["tekst"].textValue() shouldBe expectedObject.innhold.tekst
        resultObject["innhold"]["link"].textValue() shouldBe expectedObject.innhold.link
        resultObject["aktiv"].asBoolean() shouldBe expectedObject.aktiv


        assertZonedDateTime(resultObject, expectedObject.aktivFremTil, "aktivFremTil")
        assertZonedDateTime(resultObject, expectedObject.opprettet, "opprettet")
        assertZonedDateTime(resultObject, expectedObject.inaktivert, "inaktivert")

        resultObject["eksternVarsling"]["sendt"].asBoolean() shouldBe expectedObject.eksternVarsling?.sendt
        resultObject["eksternVarsling"]["sendtSomBatch"].asBoolean() shouldBe expectedObject.eksternVarsling?.sendtSomBatch
        assertZonedDateTime(resultObject["eksternVarsling"], expectedObject.eksternVarsling?.sendtTidspunkt, "sendtTidspunkt")

        resultObject["eksternVarsling"]["renotifikasjonSendt"].asBoolean() shouldBe expectedObject.eksternVarsling?.renotifikasjonSendt
        assertZonedDateTime(resultObject["eksternVarsling"], expectedObject.eksternVarsling?.renotifikasjonTidspunkt, "renotifikasjonTidspunkt")
        resultObject["eksternVarsling"]["feilhistorikk"]?.size() shouldBe expectedObject.eksternVarsling?.feilhistorikk?.size
        assertZonedDateTime(resultObject["eksternVarsling"], expectedObject.eksternVarsling?.sistOppdatert, "sistOppdatert")
    }
}
