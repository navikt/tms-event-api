package no.nav.tms.event.api.config

import no.nav.tms.token.support.azure.exchange.AzureService

class AzureTokenFetcher(private val azureService: AzureService, private val eventHandlerClientId: String) {

    suspend fun fetchTokenForEventHandler(): String = azureService.getAccessToken(eventHandlerClientId)
}
