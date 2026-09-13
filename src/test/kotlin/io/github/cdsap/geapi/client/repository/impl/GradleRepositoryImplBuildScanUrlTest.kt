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
    fun `getBuildScans without buildId caps maxBuilds and uses reverse`() {
        val captured =
            captureRequestedUrl { repository ->
                repository.getBuildScans(Filter(maxBuilds = 50))
            }

        assertEquals(
            "https://ge.example/api/builds?maxBuilds=50&reverse=true",
            captured,
        )
    }

    @Test
    fun `getBuildScans with buildId includes fromBuild before maxBuilds`() {
        val captured =
            captureRequestedUrl { repository ->
                repository.getBuildScans(Filter(maxBuilds = 100), buildId = "build-123")
            }

        assertEquals(
            "https://ge.example/api/builds?fromBuild=build-123&maxBuilds=100&reverse=true",
            captured,
        )
    }

    @Test
    fun `getBuildScans clamps maxBuilds at 1000`() {
        val captured =
            captureRequestedUrl { repository ->
                repository.getBuildScans(Filter(maxBuilds = 2500), buildId = "build-abc")
            }

        assertEquals(
            "https://ge.example/api/builds?fromBuild=build-abc&maxBuilds=1000&reverse=true",
            captured,
        )
    }

    @Test
    fun `getBuildScansWithAdvancedQuery appends query after reverse`() {
        val captured =
            captureRequestedUrl { repository ->
                repository.getBuildScansWithAdvancedQuery(
                    Filter(maxBuilds = 50),
                    query = "project:demo",
                )
            }

        assertEquals(
            "https://ge.example/api/builds?maxBuilds=50&reverse=true&query=project:demo",
            captured,
        )
    }

    @Test
    fun `getBuildScansWithAdvancedQuery preserves fromBuild maxBuilds reverse and query order`() {
        val captured =
            captureRequestedUrl { repository ->
                repository.getBuildScansWithAdvancedQuery(
                    Filter(maxBuilds = 1500),
                    query = "tag:\"ci\"",
                    buildId = "prev-build",
                )
            }

        assertEquals(
            "https://ge.example/api/builds?fromBuild=prev-build&maxBuilds=1000&reverse=true&query=tag:\"ci\"",
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
