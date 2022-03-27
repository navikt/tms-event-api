package no.nav.tms.event.api.beskjed

import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.tms.event.api.common.AzureToken
import no.nav.tms.event.api.common.AzureTokenFetcher
import no.nav.tms.event.api.common.InnloggetBrukerObjectMother
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BeskjedEventServiceTest {

    private val beskjedConsumer: BeskjedConsumer = mockk()
    private val tokenFetcher: AzureTokenFetcher = mockk()

    private val beskjedEventService = BeskjedEventService(beskjedConsumer, tokenFetcher)
    private val bruker = InnloggetBrukerObjectMother.createInnloggetBruker("123")

    private val azureToken = AzureToken("tokenValue")

    private val mockedEvents: List<Beskjed> = mockk()

    @AfterEach
    fun cleanUp() {
        clearMocks(beskjedConsumer, tokenFetcher)
    }

    @Test
    fun `should request an azure token and make request on behalf of user for active beskjed events`() {
        coEvery {
            tokenFetcher.fetchTokenForEventHandler()
        } returns azureToken

        coEvery {
            beskjedConsumer.getActiveEvents(azureToken, bruker.fodselsnummer)
        } returns mockedEvents

        val result = runBlocking {
            beskjedEventService.getActiveCachedEventsForUser(bruker)
        }

        result `should be equal to` mockedEvents

        coVerify(exactly = 1) { tokenFetcher.fetchTokenForEventHandler() }
        coVerify(exactly = 1) { beskjedConsumer.getActiveEvents(azureToken, bruker.fodselsnummer) }
    }

    @Test
    fun `should request an azure token and make request on behalf of user for inactive beskjed events`() {
        coEvery {
            tokenFetcher.fetchTokenForEventHandler()
        } returns azureToken

        coEvery {
            beskjedConsumer.getInactiveEvents(azureToken, bruker.fodselsnummer)
        } returns mockedEvents

        val result = runBlocking {
            beskjedEventService.getInactiveCachedEventsForUser(bruker)
        }

        result `should be equal to` mockedEvents

        coVerify(exactly = 1) { tokenFetcher.fetchTokenForEventHandler() }
        coVerify(exactly = 1) { beskjedConsumer.getInactiveEvents(azureToken, bruker.fodselsnummer) }
    }

    @Test
    fun `should request an azure token and make request on behalf of user for all beskjed events`() {
        coEvery {
            tokenFetcher.fetchTokenForEventHandler()
        } returns azureToken

        coEvery {
            beskjedConsumer.getAllEvents(azureToken, bruker.fodselsnummer)
        } returns mockedEvents

        val result = runBlocking {
            beskjedEventService.getAllCachedEventsForUser(bruker)
        }

        result `should be equal to` mockedEvents

        coVerify(exactly = 1) { tokenFetcher.fetchTokenForEventHandler() }
        coVerify(exactly = 1) { beskjedConsumer.getAllEvents(azureToken, bruker.fodselsnummer) }
    }

}
