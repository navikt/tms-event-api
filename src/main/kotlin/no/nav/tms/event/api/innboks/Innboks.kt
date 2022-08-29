@file:UseSerializers(ZonedDateTimeSerializer::class)

package no.nav.tms.event.api.innboks

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import no.nav.tms.event.api.common.serializer.ZonedDateTimeSerializer
import no.nav.tms.event.api.varsel.VarselDTO
import java.time.ZonedDateTime

@Serializable
data class Innboks(
    val produsent: String,
    val forstBehandlet: ZonedDateTime,
    val fodselsnummer: String,
    val eventId: String,
    val grupperingsId: String,
    val tekst: String,
    val link: String,
    val sikkerhetsnivaa: Int,
    val sistOppdatert: ZonedDateTime,
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
