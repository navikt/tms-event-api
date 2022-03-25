package no.nav.tms.template.config

import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

suspend inline fun <reified T> HttpClient.get(url: URL): T = withContext(Dispatchers.IO) {
    request {
        url("$url")
        method = HttpMethod.Get
    }
}

suspend inline fun <reified T> HttpClient.post(url: URL): T = withContext(Dispatchers.IO) {
    request {
        url("$url")
        method = HttpMethod.Post
    }
}
