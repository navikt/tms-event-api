package no.nav.tms.template.health

data class HealthStatus(
        val serviceName: String,
        val status: Status,
        val statusMessage: String,
        val includeInReadiness: Boolean = true
)

enum class Status {
    OK, ERROR
}
