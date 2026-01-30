@file:UseSerializers(ZonedDateTimeSerializer::class)

package no.nav.tms.event.api.varsel

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import no.nav.tms.event.api.config.ZonedDateTimeSerializer
import java.time.ZonedDateTime

@Serializable
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

@Serializable
data class Innhold(
    val tekst: String,
    val link: String? = null,
)

@Serializable
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

@Serializable
data class Produsent(
    val namespace: String,
    val appnavn: String,
)

@Serializable
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

@Serializable
data class EksternFeilHistorikkEntry(
    val feilmelding: String,
    val tidspunkt: ZonedDateTime
)
