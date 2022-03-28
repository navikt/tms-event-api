package no.nav.tms.event.api.innboks

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.tms.event.api.common.respondWithError
import no.nav.tms.event.api.config.doIfValidRequest
import org.slf4j.LoggerFactory

fun Route.innboksApi(innboksEventService: InnboksEventService) {

    val log = LoggerFactory.getLogger(InnboksEventService::class.java)

    get("/fetch/innboks/aktive") {
        doIfValidRequest { userToFetchEventsFor ->
            try {
                val aktiveInnboksEvents = innboksEventService.getActiveCachedEventsForUser(userToFetchEventsFor)
                call.respond(HttpStatusCode.OK, aktiveInnboksEvents)

            } catch (exception: Exception) {
                respondWithError(call, log, exception)
            }
        }
    }


    get("/fetch/innboks/inaktive") {
        doIfValidRequest { userToFetchEventsFor ->
            try {
                val inaktiveInnboksEvents = innboksEventService.getInactiveCachedEventsForUser(userToFetchEventsFor)
                call.respond(HttpStatusCode.OK, inaktiveInnboksEvents)

            } catch (exception: Exception) {
                respondWithError(call, log, exception)
            }
        }
    }

    get("/fetch/innboks/all") {
        doIfValidRequest { userToFetchEventsFor ->
            try {
                val innboksEvents = innboksEventService.getAllCachedEventsForUser(userToFetchEventsFor)
                call.respond(HttpStatusCode.OK, innboksEvents)

            } catch (exception: Exception) {
                respondWithError(call, log, exception)
            }
        }
    }
}
