package io.github.cdsap.geapi.client.network

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.contentType
import io.ktor.serialization.gson.gson

class GEClient(
    private val token: String,
    geServer: String,
    private val clientConf: ClientConf = ClientConf(),
    httpClient: HttpClient? = null,
) {
    val client = httpClient ?: createHttpClient()
    val url = if (geServer.last().toString() == "/") "${geServer.dropLast(1)}/api/builds" else "$geServer/api/builds"

    private fun createHttpClient() =
        HttpClient(CIO) {
            // Handle non-2xx in get() so callers receive GeApiHttpException with a body preview.
            expectSuccess = false
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
                    maxDelayMs = clientConf.exponentialMaxDelay,
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
        val response = client.get(url)
        val bodyText = response.bodyAsText()
        val requestUrl =
            response.call.request.url
                .toString()
        GeApiResponseValidation.validateSuccessfulJsonResponse(
            status = response.status,
            requestUrl = requestUrl,
            contentType = response.contentType(),
            bodyText = bodyText,
        )
        return gson.fromJson(bodyText, object : TypeToken<T>() {}.type)
    }

    companion object {
        @PublishedApi
        internal val gson = Gson()
    }
}

data class ClientConf(
    val maxRetries: Int = 200,
    val exponentialBase: Double = 2.0,
    val exponentialMaxDelay: Long = 60000,
)
