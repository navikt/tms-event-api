package no.nav.tms.event.api

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import nav.no.tms.common.testutils.initExternalServices
import no.nav.tms.event.api.varsel.*
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.ZonedDateTime

class ApiTest {
    @Test
    fun `Henter aktive varsler fra tms-varsel-authority `(type: String) {
        /*
        val (aktiveMockresponse, aktiveExpectedResult) =
            mockContent(
                ZonedDateTime.now().minusDays(1), ZonedDateTime.now(), ZonedDateTime.now().plusDays(10), 5,
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
    fun `Henter inaktive varsler fra tms-varsel-authority og gjør de om til DTO`(type: String) {
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
    fun `Henter alle varsler fra tms-varsel-authority og gjør de om til DTO`(type: String) {
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
    }*/
}
