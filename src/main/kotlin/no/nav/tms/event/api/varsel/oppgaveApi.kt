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
    val aktiveVarslerEndpoint = "fetch/modia/oppgave/aktive"
    val inaktiveVarslerEndpoint = "fetch/modia/oppgave/inaktive"
    val alleVarslerEndpoint = "fetch/modia/oppgave/all"

    val log = LoggerFactory.getLogger(VarselReader::class.java)

    get("/oppgave/aktive") {
        doIfValidRequest { fnr ->
            try {
                val aktiveOppgaveEvents = varselReader.fetchVarsel(fnr, aktiveVarslerEndpoint)
                call.respond(HttpStatusCode.OK, aktiveOppgaveEvents)
            } catch (exception: Exception) {
                respondWithError(call, log, exception)
            }
        }
    }

    get("/oppgave/inaktive") {
        doIfValidRequest { fnr ->
            try {
                val inaktiveOppgaveEvents = varselReader.fetchVarsel(fnr, inaktiveVarslerEndpoint)
                call.respond(HttpStatusCode.OK, inaktiveOppgaveEvents)
            } catch (exception: Exception) {
                respondWithError(call, log, exception)
            }
        }
    }

    get("/oppgave/all") {
        doIfValidRequest { fnr ->
            try {
                val oppgaveEvents = varselReader.fetchVarsel(fnr, alleVarslerEndpoint)
                call.respond(HttpStatusCode.OK, oppgaveEvents)
            } catch (exception: Exception) {
                respondWithError(call, log, exception)
            }
        }
    }
}
