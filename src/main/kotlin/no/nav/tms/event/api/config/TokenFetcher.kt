package no.nav.tms.event.api.config

import no.nav.tms.token.support.entraid.token.fetcher.EntraIdTokenFetcher

class TokenFetcher(private val tokenFetcher: EntraIdTokenFetcher, private val varselAuthorityClientId: String) {
    suspend fun fetchTokenForVarselAuthority(): String = tokenFetcher.getAccessToken(varselAuthorityClientId)
}
