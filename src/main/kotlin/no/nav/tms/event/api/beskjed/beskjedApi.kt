package no.nav.tms.event.api.beskjed

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import no.nav.tms.event.api.common.respondWithError
import no.nav.tms.event.api.config.doIfValidRequest
import org.slf4j.LoggerFactory

fun Route.beskjedApi(beskjedEventService: BeskjedEventService) {

    val log = LoggerFactory.getLogger(BeskjedEventService::class.java)

    get("/beskjed/aktive") {
        doIfValidRequest { fnr ->
            try {
                val aktiveBeskjedEvents = beskjedEventService.getActiveCachedEventsForUser(fnr)
                call.respond(HttpStatusCode.OK, aktiveBeskjedEvents)
            } catch (exception: Exception) {
                respondWithError(call, log, exception)
            }
        }
    }

    get("/beskjed/inaktive") {
        doIfValidRequest { fnr ->
            try {
                val inaktiveBeskjedEvents = beskjedEventService.getInactiveCachedEventsForUser(fnr)
                call.respond(HttpStatusCode.OK, inaktiveBeskjedEvents)
            } catch (exception: Exception) {
                respondWithError(call, log, exception)
            }
        }
    }

    get("/beskjed/all") {
        doIfValidRequest { fnr ->
            try {
                val beskjedEvents = beskjedEventService.getAllCachedEventsForUser(fnr)
                call.respond(HttpStatusCode.OK, beskjedEvents)
            } catch (exception: Exception) {
                respondWithError(call, log, exception)
            }
        }
    }
}
