package no.nav.tms.event.api.oppgave

import no.nav.tms.event.api.common.AzureTokenFetcher

class OppgaveReader(
    private val oppgaveConsumer: OppgaveConsumer,
    private val azureTokenFetcher: AzureTokenFetcher
) {

    suspend fun aktiveVarsler(fnr: String): List<OppgaveDTO> {
        val azureToken = azureTokenFetcher.fetchTokenForEventHandler()

        return oppgaveConsumer.getActiveEvents(azureToken, fnr).let {
            OppgaveTransformer.toOppgaveDTO(it)
        }
    }

    suspend fun inaktiveVarsler(fnr: String): List<OppgaveDTO> {
        val azureToken = azureTokenFetcher.fetchTokenForEventHandler()

        return oppgaveConsumer.getInactiveEvents(azureToken, fnr).let {
            OppgaveTransformer.toOppgaveDTO(it)
        }
    }

    suspend fun alleVarsler(fnr: String): List<OppgaveDTO> {
        val azureToken = azureTokenFetcher.fetchTokenForEventHandler()

        return oppgaveConsumer.getAllEvents(azureToken, fnr).let {
            OppgaveTransformer.toOppgaveDTO(it)
        }
    }
}
