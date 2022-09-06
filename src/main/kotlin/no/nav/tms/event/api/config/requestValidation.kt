package no.nav.tms.event.api.config

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.util.pipeline.PipelineContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val log = LoggerFactory.getLogger("requestValidation.tk")

suspend inline fun PipelineContext<Unit, ApplicationCall>.doIfValidRequest(handler: (fnr: String) -> Unit) {
    val headerName = "fodselsnummer"
    val fnrHeader = call.request.headers[headerName]
    when {
        fnrHeader == null -> respondWithBadRequest("Requesten mangler header-en '$headerName'")
        !isFodselsnummerOfValidLength(fnrHeader) -> respondWithBadRequest("Header-en '$headerName' inneholder ikke et gyldig fødselsnummer.")
        else -> handler.invoke(fnrHeader)
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.respondWithBadRequest(msg: String) {
    log.warn(msg)
    call.respond(HttpStatusCode.BadRequest, msg)
}

fun isFodselsnummerOfValidLength(fnrHeader: String) = fnrHeader.isNotEmpty() && fnrHeader.length == 11

suspend fun respondWithError(call: ApplicationCall, log: Logger, exception: Exception) {
    call.respond(HttpStatusCode.InternalServerError)
    log.error("Ukjent feil oppstod ved henting av eventer fra cache. Returnerer feilkode.", exception)
}
