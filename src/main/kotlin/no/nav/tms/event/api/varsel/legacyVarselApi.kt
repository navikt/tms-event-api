package no.nav.tms.event.api.varsel

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.tms.event.api.config.doIfValidHeader
import java.lang.IllegalArgumentException

private val ApplicationCall.varselTypeParam: String
    get() =
        parameters["varseltype"]?.let { varselTypeParam ->
            if (!listOf("innboks", "beskjed", "oppgave").contains(varselTypeParam)) {
                throw IllegalArgumentException("Ukjent varsetype '$varselTypeParam' i url")
            }
            varselTypeParam
        } ?: throw IllegalStateException("varseltype finnes ikke i url")

fun Route.legacyVarselApi(varselReader: VarselReader) {
    get("/{varseltype}/aktive") {
        doIfValidHeader { fnr ->

            call.respond(
                HttpStatusCode.OK,
                varselReader.fetchVarsel(fnr, "${call.varselTypeParam}/detaljert/aktive").toLegacyVarsler(),
            )
        }
    }

    get("/{varseltype}/inaktive") {
        doIfValidHeader { fnr ->
            call.respond(
                HttpStatusCode.OK,
                varselReader.fetchVarsel(fnr, "${call.varselTypeParam}/detaljert/inaktive").toLegacyVarsler(),
            )
        }
    }

    get("/{varseltype}/all") {
        doIfValidHeader { fnr ->
            call.respond(
                HttpStatusCode.OK,
                varselReader.fetchVarsel(fnr, "${call.varselTypeParam}/detaljert/alle").toLegacyVarsler(),
            )
        }
    }
}
