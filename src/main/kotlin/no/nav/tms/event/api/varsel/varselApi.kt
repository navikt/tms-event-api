package no.nav.tms.event.api.varsel

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.tms.event.api.config.doIfValidRequest

fun Route.varselApi(varselReader: VarselReader) {
    val aktiveVarslerPath = "varsel/detaljert/aktive"
    val inaktiveVarslerPath = "varsel/detaljert/inaktive"
    val alleVarslerPath = "varsel/detaljert/alle"

    get("/varsel/aktive") {
        doIfValidRequest { fnr ->
            call.respond(HttpStatusCode.OK, varselReader.fetchVarsel(fnr, aktiveVarslerPath))
        }
    }

    get("/varsel/inaktive") {
        doIfValidRequest { fnr ->
            call.respond(HttpStatusCode.OK, varselReader.fetchVarsel(fnr, inaktiveVarslerPath))
        }
    }

    get("/varsel/alle") {
        doIfValidRequest { fnr ->
            call.respond(HttpStatusCode.OK, varselReader.fetchVarsel(fnr, alleVarslerPath))
        }
    }
}
