package no.nav.tms.event.api.config

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.util.pipeline.PipelineContext
import no.nav.tms.event.api.common.User
import org.slf4j.LoggerFactory

val log = LoggerFactory.getLogger("requestValidation.tk")

suspend inline fun PipelineContext<Unit, ApplicationCall>.doIfValidRequest(handler: (fnr: User) -> Unit) {
    val headerName = "fodselsnummer"
    val fnrHeader = call.request.headers[headerName]

    if (fnrHeader != null) {
        if (isFodselsnummerOfValidLength(fnrHeader)) {
            val user = User(fnrHeader)
            handler.invoke(user)
        } else {
            val msg = "Header-en '$headerName' inneholder ikke et gyldig fødselsnummer."
            log.warn(msg)
            call.respond(HttpStatusCode.BadRequest, msg)
        }
    } else {
        val msg = "Requesten mangler header-en '$headerName'"
        log.warn(msg)
        call.respond(HttpStatusCode.BadRequest, msg)
    }
}

fun isFodselsnummerOfValidLength(fnrHeader: String) = fnrHeader.isNotEmpty() && fnrHeader.length == 11
