package no.nav.tms.event.api.oppgave

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import no.nav.tms.event.api.common.respondWithError
import no.nav.tms.event.api.config.doIfValidRequest
import org.slf4j.LoggerFactory

fun Route.oppgaveApi(oppgaveReader: OppgaveReader) {

    val log = LoggerFactory.getLogger(OppgaveReader::class.java)

    get("/oppgave/aktive") {
        doIfValidRequest { fnr ->
            try {
                val aktiveOppgaveEvents = oppgaveReader.aktiveVarsler(fnr)
                call.respond(HttpStatusCode.OK, aktiveOppgaveEvents)
            } catch (exception: Exception) {
                respondWithError(call, log, exception)
            }
        }
    }

    get("/oppgave/inaktive") {
        doIfValidRequest { fnr ->
            try {
                val inaktiveOppgaveEvents = oppgaveReader.inaktiveVarsler(fnr)
                call.respond(HttpStatusCode.OK, inaktiveOppgaveEvents)
            } catch (exception: Exception) {
                respondWithError(call, log, exception)
            }
        }
    }

    get("/oppgave/all") {
        doIfValidRequest { fnr ->
            try {
                val oppgaveEvents = oppgaveReader.alleVarsler(fnr)
                call.respond(HttpStatusCode.OK, oppgaveEvents)
            } catch (exception: Exception) {
                respondWithError(call, log, exception)
            }
        }
    }
}
