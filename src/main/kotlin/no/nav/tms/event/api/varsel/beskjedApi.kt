package no.nav.tms.event.api.varsel

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import no.nav.tms.event.api.config.doIfValidRequest
import no.nav.tms.event.api.config.respondWithError
import org.slf4j.LoggerFactory

fun Route.beskjedApi(varselReader: VarselReader) {

    val aktiveVarslerPath = "fetch/modia/beskjed/aktive"
    val inaktiveVarslerPath = "fetch/modia/beskjed/inaktive"
    val alleVarslerPath = "fetch/modia/beskjed/all"

    val log = LoggerFactory.getLogger(VarselReader::class.java)

    get("/beskjed/aktive") {
        doIfValidRequest { fnr ->
            try {
                call.respond(HttpStatusCode.OK, varselReader.fetchVarsel(fnr, aktiveVarslerPath))
            } catch (exception: Exception) {
                respondWithError(call, log, exception)
            }
        }
    }

    get("/beskjed/inaktive") {
        doIfValidRequest { fnr ->
            try {
                call.respond(HttpStatusCode.OK, varselReader.fetchVarsel(fnr, inaktiveVarslerPath))
            } catch (exception: Exception) {
                respondWithError(call, log, exception)
            }
        }
    }

    get("/beskjed/all") {
        doIfValidRequest { fnr ->
            try {
                call.respond(HttpStatusCode.OK, varselReader.fetchVarsel(fnr, alleVarslerPath))
            } catch (exception: Exception) {
                respondWithError(call, log, exception)
            }
        }
    }
}
