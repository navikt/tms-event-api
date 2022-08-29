@file:UseSerializers(ZonedDateTimeSerializer::class)

package no.nav.tms.event.api.oppgave

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import no.nav.tms.event.api.common.serializer.ZonedDateTimeSerializer
import no.nav.tms.event.api.varsel.VarselDTO
import java.time.ZonedDateTime

@Serializable
data class Oppgave(
    val fodselsnummer: String,
    val grupperingsId: String,
    val eventId: String,
    val forstBehandlet: ZonedDateTime,
    val produsent: String,
    val sikkerhetsnivaa: Int,
    val sistOppdatert: ZonedDateTime,
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