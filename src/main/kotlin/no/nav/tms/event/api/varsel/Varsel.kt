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
    val eksternVarslingSendt: Boolean,
    val eksternVarslingKanaler: List<String>
) {
    internal fun toDTO() = VarselDTO(
        forstBehandlet = forstBehandlet,
        eventId = eventId,
        fodselsnummer = fodselsnummer,
        tekst = tekst,
        link = link,
        produsent = produsent,
        sistOppdatert = sistOppdatert,
        sikkerhetsnivaa = sikkerhetsnivaa,
        aktiv = aktiv,
        grupperingsId = grupperingsId
    )
}

@Serializable
data class VarselDTO(
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
    val aktiv: Boolean
)
