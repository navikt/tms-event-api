package no.nav.tms.template.config

import io.ktor.application.*
import io.ktor.client.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.serialization.*
import no.nav.tms.template.health.healthApi

fun Application.mainModule(appContext: ApplicationContext = ApplicationContext()) {
    val environment = Environment()

    install(DefaultHeaders)

    install(CORS) {
        host(environment.corsAllowedOrigins)
        allowCredentials = true
        header(HttpHeaders.ContentType)
    }

    install(ContentNegotiation) {
        json(jsonConfig())
    }

    routing {
        healthApi(appContext.healthService)
    }

    configureShutdownHook(appContext.httpClient)
}

private fun Application.configureShutdownHook(httpClient: HttpClient) {
    environment.monitor.subscribe(ApplicationStopping) {
        httpClient.close()
    }
}
