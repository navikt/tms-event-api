package no.nav.tms.event.api.common

import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should not contain`
import org.junit.jupiter.api.Test

internal class UserTest {

    @Test
    fun `should return expected values`() {
        val expectedIdent = "12345"

        val userToFetchEventsFor = InnloggetBrukerObjectMother.createInnloggetBruker(expectedIdent)

        userToFetchEventsFor.fodselsnummer `should be equal to` expectedIdent
    }

    @Test
    fun `should not include sensitive values in the output for the toString method`() {
        val userToFetchEventsFor = InnloggetBrukerObjectMother.createInnloggetBruker()

        val outputOfToString = userToFetchEventsFor.toString()

        outputOfToString `should not contain` (userToFetchEventsFor.fodselsnummer)
    }
}
