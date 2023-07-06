package no.nav.tms.event.api.varsel

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.tms.event.api.config.doIfValidRequest

fun Route.oppgaveApi(varselReader: VarselReader) {
    val aktiveVarslerPath = "oppgave/detaljert/aktive"
    val inaktiveVarslerPath = "oppgave/detaljert/inaktive"
    val alleVarslerPath = "oppgave/detaljert/alle"

    get("/oppgave/aktive") {
        doIfValidRequest { fnr ->
            val aktiveOppgaveEvents = varselReader.fetchVarsel(fnr, aktiveVarslerPath).toLegacyVarsler()
            call.respond(HttpStatusCode.OK, aktiveOppgaveEvents)
        }
    }

    get("/oppgave/inaktive") {
        doIfValidRequest { fnr ->
            val inaktiveOppgaveEvents = varselReader.fetchVarsel(fnr, inaktiveVarslerPath).toLegacyVarsler()
            call.respond(HttpStatusCode.OK, inaktiveOppgaveEvents)
        }
    }

    get("/oppgave/all") {
        doIfValidRequest { fnr ->
            val oppgaveEvents = varselReader.fetchVarsel(fnr, alleVarslerPath).toLegacyVarsler()
            call.respond(HttpStatusCode.OK, oppgaveEvents)
        }
    }
}
