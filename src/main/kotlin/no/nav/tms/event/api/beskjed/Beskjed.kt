@file:UseSerializers(ZonedDateTimeSerializer::class)

package no.nav.tms.event.api.beskjed

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import no.nav.tms.event.api.common.serializer.ZonedDateTimeSerializer
import no.nav.tms.event.api.varsel.VarselDTO
import java.time.ZonedDateTime

@Serializable
data class Beskjed(
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