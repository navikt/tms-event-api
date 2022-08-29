package no.nav.tms.event.api.innboks

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nav.tms.event.api.common.AzureToken
import no.nav.tms.event.api.common.AzureTokenFetcher
import no.nav.tms.event.api.createListFromObject
import no.nav.tms.event.api.mockClient
import no.nav.tms.event.api.varsel.Varsel
import no.nav.tms.event.api.varsel.VarselDTO
import no.nav.tms.event.api.varsel.VarselReader
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.ZonedDateTime

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InnboksVarselReaderTest {

    private val tokenFetcher: AzureTokenFetcher = mockk()
    private val fnr = "123"
    private val azureToken = AzureToken("tokenValue")

    @Test
    fun `skal hente aktive varsler`() {
        val (mockresponse, expectedResult) = mockContent(
            ZonedDateTime.now().minusDays(1),
            ZonedDateTime.now()
        )

        coEvery {
            tokenFetcher.fetchTokenForEventHandler()
        } returns azureToken

        val result = runBlocking {
            InnboksVarselReader(
                varselReader = VarselReader(
                    tokenFetcher,
                    mockClient(Json.encodeToString(mockresponse))
                ),
                "https://no.na.ne"
            ).aktiveVarsler(fnr)
        }

        result `should be equal to` expectedResult
    }

    @Test
    fun `skal hente inaktive varsler`() {
        val (mockresponse, expectedResult) = mockContent(
            ZonedDateTime.now().minusDays(1),
            ZonedDateTime.now()
        )

        coEvery {
            tokenFetcher.fetchTokenForEventHandler()
        } returns azureToken

        val result = runBlocking {
            InnboksVarselReader(
                varselReader = VarselReader(
                    tokenFetcher,
                    mockClient(Json.encodeToString(mockresponse))
                ),
                "https://no.na.ne"
            ).inaktiveVarsler(fnr)
        }

        result `should be equal to` expectedResult
    }

    @Test
    fun `skal hente alle varsler`() {
        val (mockresponse, expectedResult) = mockContent(
            ZonedDateTime.now().minusDays(1),
            ZonedDateTime.now()
        )

        coEvery {
            tokenFetcher.fetchTokenForEventHandler()
        } returns azureToken

        val result = runBlocking {
            InnboksVarselReader(
                VarselReader(
                    tokenFetcher,
                    mockClient(Json.encodeToString(mockresponse))
                ),
                "https://no.na.ne"
            ).alleVarsler(fnr)
        }

        result `should be equal to` expectedResult
    }
}

private fun mockContent(
    førstBehandlet: ZonedDateTime,
    sistOppdatert: ZonedDateTime
): Pair<List<Varsel>, List<VarselDTO>> {
    return Pair(
        Varsel(
            fodselsnummer = "123",
            grupperingsId = "",
            eventId = "",
            forstBehandlet = førstBehandlet,
            produsent = "",
            sikkerhetsnivaa = 0,
            sistOppdatert = sistOppdatert,
            tekst = "Tadda vi tester",
            link = "",
            aktiv = false,
            eksternVarslingSendt = false,
            eksternVarslingKanaler = listOf()
        ).createListFromObject(5),
        VarselDTO(
            fodselsnummer = "123",
            grupperingsId = "",
            eventId = "",
            forstBehandlet = førstBehandlet.withFixedOffsetZone(),
            produsent = "",
            sikkerhetsnivaa = 0,
            sistOppdatert = sistOppdatert.withFixedOffsetZone(),
            tekst = "Tadda vi tester",
            link = "",
            aktiv = false
        ).createListFromObject(5)

    )
}
