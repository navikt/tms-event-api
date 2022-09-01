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
    val log = LoggerFactory.getLogger(VarselReader::class.java)
    val aktiveVarslerEndpoint = "fetch/modia/innboks/aktive"
    val inaktiveVarslerEndpoint = "fetch/modia/innboks/inaktive"
    val alleVarslerEndpoint = "fetch/modia/innboks/all"
    get("/innboks/aktive") {
        doIfValidRequest { fnr ->
            try {
                call.respond(HttpStatusCode.OK, varselReader.fetchVarsel(fnr, aktiveVarslerEndpoint))
            } catch (exception: Exception) {
                respondWithError(call, log, exception)
            }
        }
    }

    get("/innboks/inaktive") {
        doIfValidRequest { fnr ->
            try {
                call.respond(HttpStatusCode.OK, varselReader.fetchVarsel(fnr, inaktiveVarslerEndpoint))
            } catch (exception: Exception) {
                respondWithError(call, log, exception)
            }
        }
    }

    get("/innboks/all") {
        doIfValidRequest { fnr ->
            try {
                call.respond(HttpStatusCode.OK, varselReader.fetchVarsel(fnr, alleVarslerEndpoint))
            } catch (exception: Exception) {
                respondWithError(call, log, exception)
            }
        }
    }
}
