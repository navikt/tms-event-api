package no.nav.tms.event.api.varsel

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import no.nav.tms.event.api.config.doIfValidRequest
import no.nav.tms.event.api.config.respondWithError
import org.slf4j.LoggerFactory

fun Route.oppgaveApi(varselReader: VarselReader) {

    val aktiveVarslerPath = "fetch/modia/oppgave/aktive"
    val inaktiveVarslerPath = "fetch/modia/oppgave/inaktive"
    val alleVarslerPath = "fetch/modia/oppgave/all"

    val log = LoggerFactory.getLogger(VarselReader::class.java)

    get("/oppgave/aktive") {
        doIfValidRequest { fnr ->
            try {
                val aktiveOppgaveEvents = varselReader.fetchVarsel(fnr, aktiveVarslerPath)
                call.respond(HttpStatusCode.OK, aktiveOppgaveEvents)
            } catch (exception: Exception) {
                respondWithError(call, log, exception)
            }
        }
    }

    get("/oppgave/inaktive") {
        doIfValidRequest { fnr ->
            try {
                val inaktiveOppgaveEvents = varselReader.fetchVarsel(fnr, inaktiveVarslerPath)
                call.respond(HttpStatusCode.OK, inaktiveOppgaveEvents)
            } catch (exception: Exception) {
                respondWithError(call, log, exception)
            }
        }
    }

    get("/oppgave/all") {
        doIfValidRequest { fnr ->
            try {
                val oppgaveEvents = varselReader.fetchVarsel(fnr, alleVarslerPath)
                call.respond(HttpStatusCode.OK, oppgaveEvents)
            } catch (exception: Exception) {
                respondWithError(call, log, exception)
            }
        }
    }
}
