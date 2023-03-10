package io.github.cdsap.geapi.client.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.gson.gson

class GEClient(private val token: String, geServer: String, private val clientConf: ClientConf = ClientConf()) {
    val client = createHttpClient()
    val url = "$geServer/api/builds"

    private fun createHttpClient() = HttpClient(CIO) {
        engine {
            requestTimeout = 0
        }
        install(ContentNegotiation) {
            gson()
        }
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = clientConf.maxRetries)
            exponentialDelay(
                base = clientConf.exponentialBase,
                maxDelayMs = clientConf.exponentialMaxDelay
            )
        }
        install(Auth) {
            bearer {
                loadTokens {
                    BearerTokens(token, "")
                }
            }
        }
    }

    suspend inline fun <reified T : Any> get(url: String): T {
        return client.get(url).body() as T
    }
}

data class ClientConf(
    val maxRetries: Int = 200,
    val exponentialBase: Double = 2.0,
    val exponentialMaxDelay: Long = 60000
)
