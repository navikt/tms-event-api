package no.nav.tms.event.api.config

import no.nav.tms.token.support.azure.exchange.AzureService

class AzureTokenFetcher(private val azureService: AzureService, private val varselAuthorityClientId: String) {
    suspend fun fetchTokenForVarselAuthority(): String = azureService.getAccessToken(varselAuthorityClientId)
}
