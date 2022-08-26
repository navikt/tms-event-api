package no.nav.tms.event.api.innboks

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
class InnboksEventServiceTest {

    private val innboksConsumer: InnboksConsumer = mockk()
    private val tokenFetcher: AzureTokenFetcher = mockk()

    private val innboksEventService = InnboksEventService(innboksConsumer, tokenFetcher)
    private val fnr = "123"

    private val azureToken = AzureToken("tokenValue")

    private val mockedEvents: List<Innboks> = mockk()
    private val transformedEvents: List<InnboksDTO> = mockk()

    @BeforeEach
    fun setupMock() {
        mockkObject(InnboksTransformer)
    }

    @AfterEach
    fun cleanUp() {
        clearMocks(innboksConsumer, tokenFetcher)
        unmockkObject(InnboksTransformer)
    }

    @Test
    fun `should request an azure token and make request on behalf of user for active innboks events`() {
        coEvery {
            tokenFetcher.fetchTokenForEventHandler()
        } returns azureToken

        coEvery {
            innboksConsumer.getActiveEvents(azureToken, fnr)
        } returns mockedEvents

        every {
            InnboksTransformer.toInnboksDTO(mockedEvents)
        } returns transformedEvents

        val result = runBlocking {
            innboksEventService.getActiveCachedEventsForUser(fnr)
        }

        result `should be equal to` transformedEvents

        verify(exactly = 1) { InnboksTransformer.toInnboksDTO(mockedEvents) }
        coVerify(exactly = 1) { tokenFetcher.fetchTokenForEventHandler() }
        coVerify(exactly = 1) { innboksConsumer.getActiveEvents(azureToken, fnr) }
    }

    @Test
    fun `should request an azure token and make request on behalf of user for inactive innboks events`() {
        coEvery {
            tokenFetcher.fetchTokenForEventHandler()
        } returns azureToken

        coEvery {
            innboksConsumer.getInactiveEvents(azureToken, fnr)
        } returns mockedEvents

        every {
            InnboksTransformer.toInnboksDTO(mockedEvents)
        } returns transformedEvents

        val result = runBlocking {
            innboksEventService.getInactiveCachedEventsForUser(fnr)
        }

        result `should be equal to` transformedEvents

        verify(exactly = 1) { InnboksTransformer.toInnboksDTO(mockedEvents) }
        coVerify(exactly = 1) { tokenFetcher.fetchTokenForEventHandler() }
        coVerify(exactly = 1) { innboksConsumer.getInactiveEvents(azureToken, fnr) }
    }

    @Test
    fun `should request an azure token and make request on behalf of user for all innboks events`() {
        coEvery {
            tokenFetcher.fetchTokenForEventHandler()
        } returns azureToken

        coEvery {
            innboksConsumer.getAllEvents(azureToken, fnr)
        } returns mockedEvents

        every {
            InnboksTransformer.toInnboksDTO(mockedEvents)
        } returns transformedEvents

        val result = runBlocking {
            innboksEventService.getAllCachedEventsForUser(fnr)
        }

        result `should be equal to` transformedEvents

        verify(exactly = 1) { InnboksTransformer.toInnboksDTO(mockedEvents) }
        coVerify(exactly = 1) { tokenFetcher.fetchTokenForEventHandler() }
        coVerify(exactly = 1) { innboksConsumer.getAllEvents(azureToken, fnr) }
    }
}
