package no.nav.tms.event.api.varsel

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.tms.event.api.config.doIfValidRequest
import org.slf4j.LoggerFactory

fun Route.innboksApi(varselReader: VarselReader) {
    val aktiveVarslerPath = "fetch/modia/innboks/aktive"
    val inaktiveVarslerPath = "fetch/modia/innboks/inaktive"
    val alleVarslerPath = "fetch/modia/innboks/all"

    val log = LoggerFactory.getLogger(VarselReader::class.java)

    get("/innboks/aktive") {
        doIfValidRequest { fnr ->
            call.respond(HttpStatusCode.OK, varselReader.fetchVarsel(fnr, aktiveVarslerPath))
        }
    }

    get("/innboks/inaktive") {
        doIfValidRequest { fnr ->
            call.respond(HttpStatusCode.OK, varselReader.fetchVarsel(fnr, inaktiveVarslerPath))
        }
    }

    get("/innboks/all") {
        doIfValidRequest { fnr ->
            call.respond(HttpStatusCode.OK, varselReader.fetchVarsel(fnr, alleVarslerPath))
        }
    }
}
