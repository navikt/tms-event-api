package no.nav.tms.event.api.varsel

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.tms.event.api.config.doIfValidRequest

fun Route.beskjedApi(varselReader: VarselReader) {
    val aktiveVarslerPath = "fetch/modia/beskjed/aktive"
    val inaktiveVarslerPath = "fetch/modia/beskjed/inaktive"
    val alleVarslerPath = "fetch/modia/beskjed/all"

    get("/beskjed/aktive") {
        doIfValidRequest { fnr ->
            call.respond(HttpStatusCode.OK, varselReader.fetchVarsel(fnr, aktiveVarslerPath))
        }
    }

    get("/beskjed/inaktive") {
        doIfValidRequest { fnr ->
            call.respond(HttpStatusCode.OK, varselReader.fetchVarsel(fnr, inaktiveVarslerPath))
        }
    }

    get("/beskjed/all") {
        doIfValidRequest { fnr ->
            call.respond(HttpStatusCode.OK, varselReader.fetchVarsel(fnr, alleVarslerPath))
        }
    }
}
