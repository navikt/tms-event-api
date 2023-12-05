package no.nav.tms.event.api.config

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*

val log = KotlinLogging.logger {}

suspend inline fun PipelineContext<Unit, ApplicationCall>.doIfValidRequest(handler: (fnr: String) -> Unit) {
    val headerName = "fodselsnummer"
    val fnrHeader = call.request.headers[headerName]
    when {
        fnrHeader == null -> respondWithBadRequest("Request til ${call.request.uri} mangler header-en '$headerName'")
        !isFodselsnummerOfValidLength(fnrHeader) -> respondWithBadRequest("'header $headerName' i request til ${call.request.uri}  inneholder ikke et gyldig fødselsnummer.")
        else -> handler.invoke(fnrHeader)
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.respondWithBadRequest(msg: String) {
    log.warn { msg }
    call.respond(HttpStatusCode.BadRequest, msg)
}

fun isFodselsnummerOfValidLength(fnrHeader: String) = fnrHeader.isNotEmpty() && fnrHeader.length == 11
