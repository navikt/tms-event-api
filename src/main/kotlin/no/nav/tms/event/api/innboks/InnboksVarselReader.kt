package no.nav.tms.event.api.innboks

import no.nav.tms.event.api.varsel.VarselDTO
import no.nav.tms.event.api.varsel.VarselReader
import java.net.URL

class InnboksVarselReader(
    private val varselReader: VarselReader,
    eventHandlerBaseURL: String
) {

    private val activeEventsEndpoint = URL("$eventHandlerBaseURL/fetch/modia/innboks/aktive")
    private val inactiveEventsEndpoint = URL("$eventHandlerBaseURL/fetch/modia/innboks/inaktive")
    private val allEventsEndpoint = URL("$eventHandlerBaseURL/fetch/modia/innboks/all")

    suspend fun aktiveVarsler(fnr: String): List<VarselDTO> = varselReader.getEksterneVarsler(fnr, activeEventsEndpoint)
    suspend fun inaktiveVarsler(fnr: String): List<VarselDTO> = varselReader.getEksterneVarsler(fnr, inactiveEventsEndpoint)
    suspend fun alleVarsler(fnr: String): List<VarselDTO> = varselReader.getEksterneVarsler(fnr, allEventsEndpoint)
}
