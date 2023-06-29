package no.nav.tms.event.api.varsel

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.tms.event.api.config.doIfValidRequest

fun Route.beskjedApi(varselReader: VarselReader) {
    val aktiveVarslerPath = "beskjed/detaljert/aktive"
    val inaktiveVarslerPath = "beskjed/detaljert/inaktive"
    val alleVarslerPath = "beskjed/detaljert/alle"

    get("/beskjed/aktive") {
        doIfValidRequest { fnr ->
            call.respond(HttpStatusCode.OK, varselReader.fetchVarsel(fnr, aktiveVarslerPath).toLegacyVarsler())
        }
    }

    get("/beskjed/inaktive") {
        doIfValidRequest { fnr ->
            call.respond(HttpStatusCode.OK, varselReader.fetchVarsel(fnr, inaktiveVarslerPath).toLegacyVarsler())
        }
    }

    get("/beskjed/all") {
        doIfValidRequest { fnr ->
            call.respond(HttpStatusCode.OK, varselReader.fetchVarsel(fnr, alleVarslerPath).toLegacyVarsler())
        }
    }
}
