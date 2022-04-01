package no.nav.tms.event.api.common

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import org.slf4j.Logger

suspend fun respondWithError(call: ApplicationCall, log: Logger, exception: Exception) {
    call.respond(HttpStatusCode.InternalServerError)
    log.error("Ukjent feil oppstod ved henting av eventer fra cache. Returnerer feilkode.", exception)
}
