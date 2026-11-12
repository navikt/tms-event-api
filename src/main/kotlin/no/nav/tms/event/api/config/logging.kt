package no.nav.tms.event.api.config

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.auth.principal
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.request.httpMethod
import io.ktor.server.request.uri
import io.ktor.server.routing.RoutingContext
import no.nav.tms.token.support.entraid.token.verification.EntraIdPrincipal
import no.nav.tms.token.support.entraid.token.verification.EntraIdUserPrincipal
import no.nav.tms.token.support.entraid.token.verification.NaisApplication

private val log = KotlinLogging.logger {  }

fun RoutingContext.logCaller() {
    val principal = call.principal<EntraIdPrincipal>()
        ?: return

    val route = call.request.uriWithoutQuery()
    val method = call.request.httpMethod.value

    val client: NaisApplication? = principal.issuedFor

    if (principal is EntraIdUserPrincipal) {
        log.info { "Mottok $method-kall til [$route] fra app $client med obo-token." }
    } else {
        log.info { "Mottok $method-kall til [$route] fra app $client med system-token." }
    }
}

private fun ApplicationRequest.uriWithoutQuery() =
    "^([^?]+)(?:\\?.*)?\$".toRegex()
        .find(uri)
        ?.destructured
        ?.component1()
        ?: uri
