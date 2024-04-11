@file:UseSerializers(ZonedDateTimeSerializer::class)

package no.nav.tms.event.api.varsel

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import no.nav.tms.event.api.config.ZonedDateTimeSerializer
import java.time.ZonedDateTime

fun List<DetaljertVarsel>.toLegacyVarsler() =
    map { varsel ->
        val eksternVarsling = varsel.eksternVarsling?.let { mapEksternVarsling(it) }

        LegacyVarsel(
            fodselsnummer = "",
            grupperingsId = "",
            eventId = varsel.varselId,
            forstBehandlet = varsel.opprettet,
            produsent = varsel.produsent.appnavn,
            sikkerhetsnivaa = varsel.sensitivitet.loginLevel(),
            sistOppdatert = varsel.inaktivert ?: varsel.opprettet,
            synligFremTil = varsel.aktivFremTil,
            tekst = varsel.innhold.tekst,
            link = varsel.innhold.link ?: "",
            aktiv = varsel.aktiv,
            eksternVarsling = eksternVarsling,
        )
    }

private fun mapEksternVarsling(eksternVarsling: EksternVarslingStatus): LegacyEksternVarsling {
    val historikk =
        eksternVarsling.historikk.map {
            LegacyEksternVarslingHistorikkEntry(
                melding = it.melding,
                status = it.status,
                distribusjonsId = it.distribusjonsId,
                kanal = it.kanal,
                renotifikasjon = it.renotifikasjon,
                tidspunkt = it.tidspunkt,
            )
        }

    return LegacyEksternVarsling(
        sendt = eksternVarsling.sendt,
        renotifikasjonSendt = eksternVarsling.renotifikasjonSendt,
        prefererteKanaler = emptyList(),
        sendteKanaler = eksternVarsling.kanaler,
        historikk = historikk,
    )
}

@Serializable
data class LegacyVarsel(
    val fodselsnummer: String,
    val grupperingsId: String,
    val eventId: String,
    val forstBehandlet: ZonedDateTime,
    val produsent: String,
    val sikkerhetsnivaa: Int,
    val sistOppdatert: ZonedDateTime,
    val synligFremTil: ZonedDateTime?,
    val tekst: String,
    val link: String,
    val aktiv: Boolean,
    val eksternVarsling: LegacyEksternVarsling?,
) {
    val eksternVarslingSendt = eksternVarsling?.sendt ?: false
    val eksternVarslingKanaler = eksternVarsling?.sendteKanaler ?: emptyList()
}

@Serializable
data class LegacyEksternVarsling(
    val sendt: Boolean,
    val renotifikasjonSendt: Boolean,
    val prefererteKanaler: List<String>,
    val sendteKanaler: List<String>,
    val historikk: List<LegacyEksternVarslingHistorikkEntry>,
)

@Serializable
data class LegacyEksternVarslingHistorikkEntry(
    val melding: String,
    val status: String,
    val distribusjonsId: Long? = null,
    val kanal: String? = null,
    val renotifikasjon: Boolean? = null,
    val tidspunkt: ZonedDateTime,
)
