package no.nav.tms.event.api.varsel

import io.ktor.http.*
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.tms.event.api.config.doIfValidBody
import no.nav.tms.event.api.config.doIfValidHeader

fun Route.varselApi(varselReader: VarselReader) {
    get("/varsel/{aktiv}") {
        doIfValidHeader { fnr ->
            varselReader.fetchVarsel(fnr, "varsel/detaljert/${call.aktivFilterFromPath()}").let {
                call.respond(HttpStatusCode.OK, it)
            }
        }
    }

    post("/varsel/{aktiv}") {
        doIfValidBody { fnr ->
            varselReader.fetchVarsel(fnr, "varsel/detaljert/${call.aktivFilterFromPath()}").let {
                call.respond(HttpStatusCode.OK, it)
            }
        }
    }
}

private val validFilterValues = listOf("alle", "aktive", "inaktive")

private fun RoutingCall.aktivFilterFromPath(): String {
    val pathSegment = pathParameters["aktiv"]?.lowercase()

    return if (pathSegment != null && validFilterValues.contains(pathSegment)) {
        pathSegment
    } else {
        throw NotFoundException("Ugyldig aktiv-filter")
    }
}

