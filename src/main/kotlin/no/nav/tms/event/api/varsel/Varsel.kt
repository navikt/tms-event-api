@file:UseSerializers(ZonedDateTimeSerializer::class)

package no.nav.tms.event.api.varsel

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import no.nav.tms.event.api.config.ZonedDateTimeSerializer
import java.time.ZonedDateTime

@Serializable
data class Varsel(
    val fodselsnummer: String,
    val grupperingsId: String,
    val eventId: String,
    val forstBehandlet: ZonedDateTime,
    val produsent: String,
    val sikkerhetsnivaa: Int,
    val sistOppdatert: ZonedDateTime,
    val synligFremTil: ZonedDateTime? = null,
    val tekst: String,
    val link: String,
    val aktiv: Boolean,
    val eksternVarsling: EksternVarsling? = null,
) {
    val eksternVarslingSendt = eksternVarsling?.sendt ?: false
    val eksternVarslingKanaler = eksternVarsling?.sendteKanaler ?: emptyList()
}

@Serializable
data class EksternVarsling(
    val sendt: Boolean,
    val renotifikasjonSendt: Boolean,
    val prefererteKanaler: List<String>,
    val sendteKanaler: List<String>,
    val historikk: List<EksternVarslingHistorikkEntry>,
)

@Serializable
data class EksternVarslingHistorikkEntry(
    val melding: String,
    val status: String,
    val distribusjonsId: Long? = null,
    val kanal: String? = null,
    val renotifikasjon: Boolean? = null,
    val tidspunkt: ZonedDateTime,
)
