package no.nav.tms.event.api.config

import io.ktor.server.application.*
import io.ktor.util.pipeline.*

inline fun PipelineContext<Unit, ApplicationCall>.doIfValidRequest(handler: (fnr: String) -> Unit) {
    val headerName = "fodselsnummer"
    val fnrHeader = call.request.headers[headerName]
    when {
        fnrHeader == null -> throw IllegalArgumentException("Request mangler header-en '$headerName'")
        !isFodselsnummerOfValidLength(
            fnrHeader,
        ) -> throw IllegalArgumentException("'header '$headerName' i request er ikke et gyldig fødselsnummer.")
        else -> handler.invoke(fnrHeader)
    }
}

fun isFodselsnummerOfValidLength(fnrHeader: String) = fnrHeader.isNotEmpty() && fnrHeader.length == 11
