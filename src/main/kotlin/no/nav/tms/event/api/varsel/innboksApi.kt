package no.nav.tms.event.api.varsel

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import no.nav.tms.event.api.config.doIfValidRequest
import no.nav.tms.event.api.config.respondWithError
import org.slf4j.LoggerFactory

fun Route.innboksApi(varselReader: VarselReader) {

    val aktiveVarslerPath = "fetch/modia/innboks/aktive"
    val inaktiveVarslerPath = "fetch/modia/innboks/inaktive"
    val alleVarslerPath = "fetch/modia/innboks/all"

    val log = LoggerFactory.getLogger(VarselReader::class.java)

    get("/innboks/aktive") {
        doIfValidRequest { fnr ->
            try {
                call.respond(HttpStatusCode.OK, varselReader.fetchVarsel(fnr, aktiveVarslerPath))
            } catch (exception: Exception) {
                respondWithError(call, log, exception)
            }
        }
    }

    get("/innboks/inaktive") {
        doIfValidRequest { fnr ->
            try {
                call.respond(HttpStatusCode.OK, varselReader.fetchVarsel(fnr, inaktiveVarslerPath))
            } catch (exception: Exception) {
                respondWithError(call, log, exception)
            }
        }
    }

    get("/innboks/all") {
        doIfValidRequest { fnr ->
            try {
                call.respond(HttpStatusCode.OK, varselReader.fetchVarsel(fnr, alleVarslerPath))
            } catch (exception: Exception) {
                respondWithError(call, log, exception)
            }
        }
    }
}
