package no.nav.tms.event.api.beskjed

import no.nav.tms.event.api.varsel.VarselDTO
import no.nav.tms.event.api.varsel.VarselReader
import java.net.URL

class BeskjedVarselReader(
    val varselReader: VarselReader,
    eventHandlerBaseURL: String
) {

    private val aktiveVarslerEndpoint = URL("$eventHandlerBaseURL/fetch/modia/beskjed/aktive")
    private val inaktiveVarslerEndpoint = URL("$eventHandlerBaseURL/fetch/modia/beskjed/inaktive")
    private val alleVarslerEndpoint = URL("$eventHandlerBaseURL/fetch/modia/beskjed/all")
    suspend fun aktiveVarsler(fnr: String): List<VarselDTO> = varselReader.getEksterneVarsler(fnr, aktiveVarslerEndpoint)

    suspend fun inaktiveVarsler(fnr: String): List<VarselDTO> = varselReader.getEksterneVarsler(fnr, inaktiveVarslerEndpoint)

    suspend fun alleVarsler(fnr: String): List<VarselDTO> = varselReader.getEksterneVarsler(fnr, alleVarslerEndpoint)
}
