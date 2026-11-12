package no.nav.tms.event.api.varsel

import io.ktor.http.*
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.tms.event.api.config.doIfValidBody
import no.nav.tms.event.api.config.doIfValidHeader
import no.nav.tms.event.api.config.logCaller
import java.time.ZonedDateTime

fun Route.varselApi(varselReader: VarselReader) {
    get("/varsel/{aktiv}") {
        doIfValidHeader { fnr ->
            logCaller()

            varselReader.fetchVarsel(fnr, "varsel/detaljert/${call.aktivFilterFromPath()}")
                .map(VarselDto::fromDetaljertVarsel)
                .let { call.respond(HttpStatusCode.OK, it) }
        }
    }

    post("/varsel/{aktiv}") {
        doIfValidBody { fnr ->
            logCaller()

            varselReader.fetchVarsel(fnr, "varsel/detaljert/${call.aktivFilterFromPath()}")
                .map(VarselDto::fromDetaljertVarsel)
                .let { call.respond(HttpStatusCode.OK, it) }
        }
    }
}

private val validFilterValues = listOf("alle", "aktive", "inaktive")

private fun RoutingCall.aktivFilterFromPath(): String {
    val pathSegment = pathParameters["aktiv"]?.lowercase()

    return if (pathSegment != null && validFilterValues.contains(pathSegment)) {
        pathSegment
    } else {
        throw NotFoundException("Ugyldig aktiv-filter")
    }
}

data class VarselDto(
    val type: String,
    val varselId: String,
    val aktiv: Boolean,
    val produsent: Produsent,
    val sensitivitet: Sensitivitet,
    val innhold: Innhold,
    val eksternVarsling: EksternVarslingDto?,
    val opprettet: ZonedDateTime,
    val aktivFremTil: ZonedDateTime?,
    val inaktivert: ZonedDateTime?,
    val inaktivertAv: String?
) {
    companion object {
        fun fromDetaljertVarsel(detaljertVarsel: DetaljertVarsel): VarselDto {
            return VarselDto(
                type = detaljertVarsel.type,
                varselId = detaljertVarsel.varselId,
                aktiv = detaljertVarsel.aktiv,
                produsent = detaljertVarsel.produsent,
                sensitivitet = detaljertVarsel.sensitivitet,
                innhold = detaljertVarsel.innhold,
                eksternVarsling = detaljertVarsel.eksternVarsling?.let(EksternVarslingDto::fromEksternVarslingStatus),
                opprettet = detaljertVarsel.opprettet,
                aktivFremTil = detaljertVarsel.aktivFremTil,
                inaktivert = detaljertVarsel.inaktivert,
                inaktivertAv = detaljertVarsel.inaktivertAv
            )
        }
    }
}

data class EksternVarslingDto(
    val sendt: Boolean,
    val sendtTidspunkt: ZonedDateTime?,
    val sendtSomBatch: Boolean,
    val kanal: String?,
    val tittel: String?,
    val tekst: String?,
    val renotifikasjonSendt: Boolean,
    val renotifikasjonTidspunkt: ZonedDateTime?,
    val renotifikasjonKanal: String?,
    val renotifikasjonTittel: String?,
    val renotifikasjonTekst: String?,
    @Deprecated("Bruk 'kanal' og 'renotifikasjonKanal'") val kanaler: List<String>,
    val feilhistorikk: List<EksternFeilHistorikkEntry>,
    val sistOppdatert: ZonedDateTime
) {
    // kompatibilitet
    val historikk: List<Unit> = emptyList()

    companion object {
        fun fromEksternVarslingStatus(eksternVarsling: EksternVarslingStatus): EksternVarslingDto {
            return if (eksternVarsling.sending != null) {
                // I praksis vil det aldri være mer enn én renotifikasjon basert på dagens oppsett, men
                // baksystemene legger opp til at det kan være et vilkårlig antall
                val sisteRenotifikasjon = eksternVarsling.renotifikasjoner
                    .sortedBy { it.tidspunkt }
                    .lastOrNull()

                EksternVarslingDto(
                    sendt = true,
                    sendtTidspunkt = eksternVarsling.sending.tidspunkt,
                    sendtSomBatch = eksternVarsling.sendtSomBatch,
                    kanal = eksternVarsling.sending.kanal,
                    tittel = eksternVarsling.sending.tittel,
                    tekst = eksternVarsling.sending.tekst,
                    renotifikasjonSendt = sisteRenotifikasjon != null,
                    renotifikasjonTidspunkt = sisteRenotifikasjon?.tidspunkt,
                    renotifikasjonKanal = sisteRenotifikasjon?.kanal,
                    renotifikasjonTittel = sisteRenotifikasjon?.tittel,
                    renotifikasjonTekst = sisteRenotifikasjon?.tekst,
                    feilhistorikk = eksternVarsling.feilhistorikk,
                    sistOppdatert = eksternVarsling.sistOppdatert,
                    kanaler = listOfNotNull(
                        eksternVarsling.sending.kanal,
                        sisteRenotifikasjon?.kanal
                    ).distinct()
                )
            } else {
                EksternVarslingDto(
                    sendt = eksternVarsling.sendt,
                    sendtTidspunkt = eksternVarsling.sendtTidspunkt,
                    sendtSomBatch = eksternVarsling.sendtSomBatch,
                    kanal = null,
                    tittel = null,
                    tekst = null,
                    renotifikasjonSendt = eksternVarsling.renotifikasjonSendt,
                    renotifikasjonTidspunkt = eksternVarsling.renotifikasjonTidspunkt,
                    renotifikasjonKanal = null,
                    renotifikasjonTittel = null,
                    renotifikasjonTekst = null,
                    kanaler = eksternVarsling.kanaler,
                    feilhistorikk = eksternVarsling.feilhistorikk,
                    sistOppdatert = eksternVarsling.sistOppdatert,
                )
            }
        }
    }
}
