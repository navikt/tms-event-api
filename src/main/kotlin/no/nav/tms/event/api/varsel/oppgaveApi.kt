package no.nav.tms.event.api.varsel

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.tms.event.api.config.doIfValidRequest
import org.slf4j.LoggerFactory

fun Route.oppgaveApi(varselReader: VarselReader) {
    val aktiveVarslerPath = "fetch/modia/oppgave/aktive"
    val inaktiveVarslerPath = "fetch/modia/oppgave/inaktive"
    val alleVarslerPath = "fetch/modia/oppgave/all"

    val log = LoggerFactory.getLogger(VarselReader::class.java)

    get("/oppgave/aktive") {
        doIfValidRequest { fnr ->
            val aktiveOppgaveEvents = varselReader.fetchVarsel(fnr, aktiveVarslerPath)
            call.respond(HttpStatusCode.OK, aktiveOppgaveEvents)
        }
    }

    get("/oppgave/inaktive") {
        doIfValidRequest { fnr ->
            val inaktiveOppgaveEvents = varselReader.fetchVarsel(fnr, inaktiveVarslerPath)
            call.respond(HttpStatusCode.OK, inaktiveOppgaveEvents)
        }
    }

    get("/oppgave/all") {
        doIfValidRequest { fnr ->
            val oppgaveEvents = varselReader.fetchVarsel(fnr, alleVarslerPath)
            call.respond(HttpStatusCode.OK, oppgaveEvents)
        }
    }
}
