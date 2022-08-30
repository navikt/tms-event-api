package no.nav.tms.event.api.health

import io.ktor.application.ApplicationCall
import io.ktor.html.respondHtml
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.coroutineScope
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.title
import kotlinx.html.tr

suspend fun ApplicationCall.buildSelftestPage(healthService: HealthService) = coroutineScope {

    val healthChecks = healthService.getHealthChecks()
    val hasFailedChecks = healthChecks.any { healthStatus -> Status.ERROR == healthStatus.status }

    respondHtml(
        status =
        if (hasFailedChecks) {
            HttpStatusCode.ServiceUnavailable
        } else {
            HttpStatusCode.OK
        }
    ) {
        head {
            title { +"Selftest tms-event-api" }
        }
        body {
            var text = if (hasFailedChecks) {
                "FEIL"
            } else {
                "Service-status: OK"
            }
            h1 {
                style = if (hasFailedChecks) {
                    "background: red;font-weight:bold"
                } else {
                    "background: green"
                }
                +text
            }
            table {
                thead {
                    tr { th { +"SELFTEST tms-event-api" } }
                }
                tbody {
                    healthChecks.map {
                        tr {
                            td { +it.serviceName }
                            td {
                                style = if (it.status == Status.OK) {
                                    "background: green"
                                } else {
                                    "background: red;font-weight:bold"
                                }
                                +it.status.toString()
                            }
                            td { +it.statusMessage }
                        }
                    }
                }
            }
        }
    }
}
