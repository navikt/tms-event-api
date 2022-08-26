package no.nav.tms.event.api.innboks

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import no.nav.tms.event.api.common.respondWithError
import no.nav.tms.event.api.config.doIfValidRequest
import org.slf4j.LoggerFactory

fun Route.innboksApi(innboksEventService: InnboksEventService) {

    val log = LoggerFactory.getLogger(InnboksEventService::class.java)

    get("/innboks/aktive") {
        doIfValidRequest { fnr ->
            try {
                val aktiveInnboksEvents = innboksEventService.getActiveCachedEventsForUser(fnr)
                call.respond(HttpStatusCode.OK, aktiveInnboksEvents)
            } catch (exception: Exception) {
                respondWithError(call, log, exception)
            }
        }
    }

    get("/innboks/inaktive") {
        doIfValidRequest { fnr ->
            try {
                val inaktiveInnboksEvents = innboksEventService.getInactiveCachedEventsForUser(fnr)
                call.respond(HttpStatusCode.OK, inaktiveInnboksEvents)
            } catch (exception: Exception) {
                respondWithError(call, log, exception)
            }
        }
    }

    get("/innboks/all") {
        doIfValidRequest { fnr ->
            try {
                val innboksEvents = innboksEventService.getAllCachedEventsForUser(fnr)
                call.respond(HttpStatusCode.OK, innboksEvents)
            } catch (exception: Exception) {
                respondWithError(call, log, exception)
            }
        }
    }
}
