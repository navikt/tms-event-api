package no.nav.tms.event.api.beskjed

object BeskjedTransformer {
    fun toBeskjedDTO(beskjedList: List<Beskjed>): List<BeskjedDTO> {
        return beskjedList.map {
            toBeskjedDTO(it)
        }
    }

    private fun toBeskjedDTO(beskjed: Beskjed): BeskjedDTO =
        beskjed.let {
            BeskjedDTO(
                forstBehandlet = it.forstBehandlet,
                eventId = it.eventId,
                fodselsnummer = it.fodselsnummer,
                tekst = it.tekst,
                link = it.link,
                produsent = it.produsent,
                sistOppdatert = it.sistOppdatert,
                sikkerhetsnivaa = it.sikkerhetsnivaa,
                aktiv = it.aktiv,
                grupperingsId = it.grupperingsId
            )
        }
}

