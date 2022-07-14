package no.nav.tms.event.api.innboks

object InnboksTransformer {
    fun toInnboksDTO(innboksList: List<Innboks>): List<InnboksDTO> {
        return innboksList.map {
            toInnboksDTO(it)
        }
    }

    private fun toInnboksDTO(innboks: Innboks): InnboksDTO =
        innboks.let {
            InnboksDTO(
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

