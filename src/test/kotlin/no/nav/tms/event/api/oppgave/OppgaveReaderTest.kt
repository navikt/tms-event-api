package no.nav.tms.event.api.oppgave

import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import no.nav.tms.event.api.common.AzureToken
import no.nav.tms.event.api.common.AzureTokenFetcher
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OppgaveReaderTest {

    private val oppgaveConsumer: OppgaveConsumer = mockk()
    private val tokenFetcher: AzureTokenFetcher = mockk()

    private val oppgaveReader = OppgaveReader(oppgaveConsumer, tokenFetcher)
    private val fnr = "123"

    private val azureToken = AzureToken("tokenValue")

    private val mockedEvents: List<Oppgave> = mockk()
    private val transformedEvents: List<OppgaveDTO> = mockk()

    @BeforeEach
    fun setupMock() {
        mockkObject(OppgaveTransformer)
    }

    @AfterEach
    fun cleanUp() {
        clearMocks(oppgaveConsumer, tokenFetcher)
        unmockkObject(OppgaveTransformer)
    }

    @Test
    fun `should request an azure token and make request on behalf of user for active oppgave events`() {
        coEvery {
            tokenFetcher.fetchTokenForEventHandler()
        } returns azureToken

        coEvery {
            oppgaveConsumer.getActiveEvents(azureToken, fnr)
        } returns mockedEvents

        every {
            OppgaveTransformer.toOppgaveDTO(mockedEvents)
        } returns transformedEvents

        val result = runBlocking {
            oppgaveReader.aktiveVarsler(fnr)
        }

        result `should be equal to` transformedEvents

        verify(exactly = 1) { OppgaveTransformer.toOppgaveDTO(mockedEvents) }
        coVerify(exactly = 1) { tokenFetcher.fetchTokenForEventHandler() }
        coVerify(exactly = 1) { oppgaveConsumer.getActiveEvents(azureToken, fnr) }
    }

    @Test
    fun `should request an azure token and make request on behalf of user for inactive oppgave events`() {
        coEvery {
            tokenFetcher.fetchTokenForEventHandler()
        } returns azureToken

        coEvery {
            oppgaveConsumer.getInactiveEvents(azureToken, fnr)
        } returns mockedEvents

        every {
            OppgaveTransformer.toOppgaveDTO(mockedEvents)
        } returns transformedEvents

        val result = runBlocking {
            oppgaveReader.inaktiveVarsler(fnr)
        }

        result `should be equal to` transformedEvents

        verify(exactly = 1) { OppgaveTransformer.toOppgaveDTO(mockedEvents) }
        coVerify(exactly = 1) { tokenFetcher.fetchTokenForEventHandler() }
        coVerify(exactly = 1) { oppgaveConsumer.getInactiveEvents(azureToken, fnr) }
    }

    @Test
    fun `should request an azure token and make request on behalf of user for all oppgave events`() {
        coEvery {
            tokenFetcher.fetchTokenForEventHandler()
        } returns azureToken

        coEvery {
            oppgaveConsumer.getAllEvents(azureToken, fnr)
        } returns mockedEvents

        every {
            OppgaveTransformer.toOppgaveDTO(mockedEvents)
        } returns transformedEvents

        val result = runBlocking {
            oppgaveReader.alleVarsler(fnr)
        }

        result `should be equal to` transformedEvents

        verify(exactly = 1) { OppgaveTransformer.toOppgaveDTO(mockedEvents) }
        coVerify(exactly = 1) { tokenFetcher.fetchTokenForEventHandler() }
        coVerify(exactly = 1) { oppgaveConsumer.getAllEvents(azureToken, fnr) }
    }
}
