package no.nav.tms.event.api.oppgave

object OppgaveTransformer {
    fun toOppgaveDTO(oppgaveList: List<Oppgave>): List<OppgaveDTO> {
        return oppgaveList.map {
            toOppgaveDTO(it)
        }
    }

    private fun toOppgaveDTO(oppgave: Oppgave): OppgaveDTO =
        oppgave.let {
            OppgaveDTO(
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

