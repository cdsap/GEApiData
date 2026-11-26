package io.github.cdsap.geapi.client.repository.impl

import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.network.GEClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GradleRepositoryImplBuildScanUrlTest {
    @Test
    fun `repository uses assembled parameters in outgoing request`() {
        val captured =
            captureRequestedUrl { repository ->
                repository.getBuildScansWithAdvancedQuery(
                    Filter(
                        maxBuilds = 1500,
                        project = "my-project",
                        tags = listOf("ci", "main"),
                        advancedQuery = "gradle.buildCache.hasError:true AND buildOutcome:failed",
                    ),
                    buildId = "prev-build",
                )
            }

        assertEquals(
            "https://ge.example/api/builds?fromBuild=prev-build&maxBuilds=1000&reverse=true" +
                "&query=project:my-project%20(tag:\"ci\"%20OR%20tag:\"main\")" +
                "%20gradle.buildCache.hasError:true%20AND%20buildOutcome:failed",
            captured,
        )
    }

    private fun captureRequestedUrl(block: suspend (GradleRepositoryImpl) -> Unit): String {
        var requestedUrl: String? = null
        val engine =
            MockEngine { request ->
                requestedUrl = request.url.toString()
                respond(
                    content = "[]",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
        val httpClient =
            HttpClient(engine) {
                expectSuccess = false
            }
        val client = GEClient(token = "test-token", geServer = "https://ge.example", httpClient = httpClient)
        val repository = GradleRepositoryImpl(client)

        runBlocking { block(repository) }

        return requestedUrl ?: error("Expected a request URL to be captured")
    }
}
