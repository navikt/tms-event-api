package no.nav.tms.event.api.config

import io.ktor.server.request.receiveNullable
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

inline fun RoutingContext.doIfValidHeader(handler: (fnr: String) -> Unit) {
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


suspend fun RoutingContext.doIfValidBody(handler: suspend (String) -> Unit) {
    val identBody: IdentBody? = call.receiveNullable()

    when {
        identBody == null -> throw IllegalArgumentException("Request mangler body { \"ident\": \"<ident>\" }")
        !isFodselsnummerOfValidLength(
            identBody.ident,
        ) -> throw IllegalArgumentException("Gitt ident er ikke et gyldig fødselsnummer.")
        else -> handler.invoke(identBody.ident)
    }
}

fun isFodselsnummerOfValidLength(fnrHeader: String) = fnrHeader.isNotEmpty() && fnrHeader.length == 11

@Serializable
private data class IdentBody(val ident: String)
