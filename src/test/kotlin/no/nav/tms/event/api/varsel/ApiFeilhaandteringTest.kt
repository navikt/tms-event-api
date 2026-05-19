package no.nav.tms.event.api.varsel

import io.kotest.matchers.shouldBe
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.testing.*
import io.mockk.mockk
import no.nav.tms.event.api.api
import no.nav.tms.event.api.config.TokenFetcher
import no.nav.tms.event.api.config.jsonConfig
import no.nav.tms.event.api.setupErrorVarselRoute
import no.nav.tms.event.api.eventApiSetup
import no.nav.tms.token.support.entraid.token.verification.mock.entraIdMock
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ApiFeilhaandteringTest {
    private val tokenFetchMock = mockk<TokenFetcher>(relaxed = true)
    private val testHostUrl = "https://www.test.no"

    @Test
    fun `håndterer feilresponse fra baksystemer`() {
        testApplication {
            val applicationClient =
                createClient {
                    jsonConfig()
                    install(HttpTimeout) {
                        requestTimeoutMillis = 2000
                    }
                }
            application {
                api(
                    varselReader =
                        VarselReader(
                            tokenFetcher = tokenFetchMock,
                            client = applicationClient,
                            varselAuthorityUrl = testHostUrl,
                        ),
                    httpClient = applicationClient,
                    authConfig = {
                        authentication {
                            entraIdMock {
                                enableDefaultAuthentication {

                                }
                            }
                        }
                    },
                )
            }

            setupErrorVarselRoute(testHostUrl, "/innboks/aktive")

            client.get("/innboks/aktive") {
                header("fodselsnummer", "12345678910")
            }.status shouldBe HttpStatusCode.ServiceUnavailable
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["beskjed", "oppgave", "innboks"])
    fun `bad request for ugyldig fødselsnummer i header`(varselType: String) {
        testApplication {
            eventApiSetup(testHostUrl)
            client.get("/detaljert/aktive").status shouldBe HttpStatusCode.BadRequest
            client.get {
                url("/$varselType/inaktive")
                header("fodselsnummer", "1234")
            }.status shouldBe HttpStatusCode.BadRequest
        }
    }
}
