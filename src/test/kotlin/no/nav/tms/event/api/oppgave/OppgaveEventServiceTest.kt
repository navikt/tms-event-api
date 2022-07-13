package no.nav.tms.event.api.oppgave

import io.mockk.*
import kotlinx.coroutines.runBlocking
import no.nav.tms.event.api.common.AzureToken
import no.nav.tms.event.api.common.AzureTokenFetcher
import no.nav.tms.event.api.common.InnloggetBrukerObjectMother
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OppgaveEventServiceTest {

    private val oppgaveConsumer: OppgaveConsumer = mockk()
    private val tokenFetcher: AzureTokenFetcher = mockk()

    private val oppgaveEventService = OppgaveEventService(oppgaveConsumer, tokenFetcher)
    private val bruker = InnloggetBrukerObjectMother.createInnloggetBruker("123")

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
            oppgaveConsumer.getActiveEvents(azureToken, bruker.fodselsnummer)
        } returns mockedEvents

        every {
            OppgaveTransformer.toOppgaveDTO(mockedEvents)
        } returns transformedEvents

        val result = runBlocking {
            oppgaveEventService.getActiveCachedEventsForUser(bruker)
        }

        result `should be equal to` transformedEvents

        verify(exactly = 1) { OppgaveTransformer.toOppgaveDTO(mockedEvents) }
        coVerify(exactly = 1) { tokenFetcher.fetchTokenForEventHandler() }
        coVerify(exactly = 1) { oppgaveConsumer.getActiveEvents(azureToken, bruker.fodselsnummer) }
    }

    @Test
    fun `should request an azure token and make request on behalf of user for inactive oppgave events`() {
        coEvery {
            tokenFetcher.fetchTokenForEventHandler()
        } returns azureToken

        coEvery {
            oppgaveConsumer.getInactiveEvents(azureToken, bruker.fodselsnummer)
        } returns mockedEvents

        every {
            OppgaveTransformer.toOppgaveDTO(mockedEvents)
        } returns transformedEvents

        val result = runBlocking {
            oppgaveEventService.getInactiveCachedEventsForUser(bruker)
        }

        result `should be equal to` transformedEvents

        verify(exactly = 1) { OppgaveTransformer.toOppgaveDTO(mockedEvents) }
        coVerify(exactly = 1) { tokenFetcher.fetchTokenForEventHandler() }
        coVerify(exactly = 1) { oppgaveConsumer.getInactiveEvents(azureToken, bruker.fodselsnummer) }
    }

    @Test
    fun `should request an azure token and make request on behalf of user for all oppgave events`() {
        coEvery {
            tokenFetcher.fetchTokenForEventHandler()
        } returns azureToken

        coEvery {
            oppgaveConsumer.getAllEvents(azureToken, bruker.fodselsnummer)
        } returns mockedEvents

        every {
            OppgaveTransformer.toOppgaveDTO(mockedEvents)
        } returns transformedEvents

        val result = runBlocking {
            oppgaveEventService.getAllCachedEventsForUser(bruker)
        }

        result `should be equal to` transformedEvents

        verify(exactly = 1) { OppgaveTransformer.toOppgaveDTO(mockedEvents) }
        coVerify(exactly = 1) { tokenFetcher.fetchTokenForEventHandler() }
        coVerify(exactly = 1) { oppgaveConsumer.getAllEvents(azureToken, bruker.fodselsnummer) }
    }
}

