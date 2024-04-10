package no.nav.tms.event.api.varsel

import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.server.auth.*
import io.ktor.server.testing.*
import io.mockk.mockk
import nav.no.tms.common.testutils.initExternalServices
import no.nav.tms.event.api.ErrorRouteProvider
import no.nav.tms.event.api.api
import no.nav.tms.event.api.config.AzureTokenFetcher
import no.nav.tms.event.api.config.jsonConfig
import no.nav.tms.event.api.eventApiSetup
import no.nav.tms.token.support.azure.validation.mock.azureMock
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ApiFeilhåndteringTest {
    private val tokenFetchMock = mockk<AzureTokenFetcher>(relaxed = true)
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
                            azureTokenFetcher = tokenFetchMock,
                            client = applicationClient,
                            varselAuthorityUrl = testHostUrl,
                        ),
                    httpClient = applicationClient,
                    authConfig = {
                        authentication {
                            azureMock {
                                alwaysAuthenticated = true
                                setAsDefault = true
                            }
                        }
                    },
                )
            }

            initExternalServices(
                testHostUrl,
                ErrorRouteProvider(endpoint = "innboks/detaljert/aktive", statusCode = InternalServerError),
            )

            client.get("/beskjed/aktive") {
                header("fodselsnummer", "12345678910")
            }.status shouldBe HttpStatusCode.ServiceUnavailable
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
            client.get("/$varselType/aktive").status shouldBe HttpStatusCode.BadRequest
            client.get {
                url("/$varselType/inaktive")
                header("fodselsnummer", "1234")
            }.status shouldBe HttpStatusCode.BadRequest
        }
    }
}
