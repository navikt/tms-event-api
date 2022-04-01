package no.nav.tms.event.api.common

import org.apache.http.ConnectionClosedException
import java.net.SocketException

inline fun <reified T> retryOnConnectionLost(retries: Int = 3, outgoingCall: () -> T): T {
    var attempts = 0

    lateinit var lastError: Exception

    while (attempts < retries) {
        try {
            return outgoingCall()
        } catch (e: ConnectionClosedException) {
            attempts++
            lastError = e
        } catch (e: SocketException) {
            attempts++
            lastError = e
        }
    }

    throw ConnectionFailedException("Klarte ikke hente data etter $attempts forsøk. Viser info for siste feil.", lastError)
}

class ConnectionFailedException(message: String, cause: Exception): Exception(message, cause)
