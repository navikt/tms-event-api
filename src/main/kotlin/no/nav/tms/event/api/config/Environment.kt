package no.nav.tms.event.api.config

import no.nav.personbruker.dittnav.common.util.config.StringEnvVar.getEnvVar

data class Environment(
    val eventHandlerUrl: String = getEnvVar("EVENT_HANDLER_URL"),
    val eventHandlerClientId: String = getEnvVar("EVENT_HANDLER_CLIENT_ID")
)
