package no.nav.tms.event.api.varsel

import java.time.ZonedDateTime

data class DetaljertVarsel(
    val type: String,
    val varselId: String,
    val aktiv: Boolean,
    val produsent: Produsent,
    val sensitivitet: Sensitivitet,
    val innhold: Innhold,
    val eksternVarsling: EksternVarslingStatus? = null,
    val opprettet: ZonedDateTime,
    val aktivFremTil: ZonedDateTime? = null,
    val inaktivert: ZonedDateTime? = null,
    val inaktivertAv: String? = null,
)

data class Innhold(
    val tekst: String,
    val link: String? = null,
)

enum class Sensitivitet {
    substantial,
    high,
    ;

    fun loginLevel() =
        when (this) {
            substantial -> 3
            high -> 4
        }
}

data class Produsent(
    val namespace: String,
    val appnavn: String,
)

data class EksternVarslingStatus(
    val sendt: Boolean,
    val sendtTidspunkt: ZonedDateTime? = null,
    val sendtSomBatch: Boolean,
    val renotifikasjonSendt: Boolean,
    val renotifikasjonTidspunkt: ZonedDateTime? = null,
    val kanaler: List<String>,
    val feilhistorikk: List<EksternFeilHistorikkEntry>,
    val sistOppdatert: ZonedDateTime
) {
    // kompatibilitet
    val historikk: List<Unit> = emptyList()
}

data class EksternFeilHistorikkEntry(
    val feilmelding: String,
    val tidspunkt: ZonedDateTime
)
