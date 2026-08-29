package io.github.cdsap.geapi.client.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GEClientHttpErrorTest {
    @Test
    fun `401 plain text throws GeApiHttpException with unauthorized hint`() {
        val client =
            geClientResponding(
                status = HttpStatusCode.Unauthorized,
                body = "Unauthorized",
                contentType = "text/plain",
            )

        val exception =
            assertThrows(GeApiHttpException::class.java) {
                runBlocking { client.get<SamplePayload>("https://ge.example/api/builds/abc") }
            }

        assertEquals(401, exception.statusCode)
        assertTrue(exception.message!!.contains("Unauthorized", ignoreCase = true))
        assertTrue(exception.message!!.contains("api-key", ignoreCase = true))
        assertTrue(exception.bodyPreview.contains("Unauthorized"))
        assertTrue(exception.requestUrl.contains("/api/builds/abc"))
    }

    @Test
    fun `403 html body includes status and snippet`() {
        val client =
            geClientResponding(
                status = HttpStatusCode.Forbidden,
                body = "<html><body>Forbidden</body></html>",
                contentType = "text/html",
            )

        val exception =
            assertThrows(GeApiHttpException::class.java) {
                runBlocking { client.get<SamplePayload>("https://ge.example/api/builds") }
            }

        assertEquals(403, exception.statusCode)
        assertTrue(exception.bodyPreview.contains("Forbidden"))
        assertTrue(exception.message!!.contains("403"))
    }

    @Test
    fun `404 includes not found hint`() {
        val client =
            geClientResponding(
                status = HttpStatusCode.NotFound,
                body = "Build not found",
                contentType = "text/plain",
            )

        val exception =
            assertThrows(GeApiHttpException::class.java) {
                runBlocking { client.get<SamplePayload>("https://ge.example/api/builds/missing") }
            }

        assertEquals(404, exception.statusCode)
        assertTrue(exception.message!!.contains("build id", ignoreCase = true))
        assertTrue(exception.bodyPreview.contains("Build not found"))
    }

    @Test
    fun `500 includes retry hint and body preview`() {
        val client =
            geClientResponding(
                status = HttpStatusCode.InternalServerError,
                body = "Internal Server Error",
                contentType = "text/plain",
            )

        val exception =
            assertThrows(GeApiHttpException::class.java) {
                runBlocking { client.get<SamplePayload>("https://ge.example/api/builds") }
            }

        assertEquals(500, exception.statusCode)
        assertTrue(exception.message!!.contains("retry", ignoreCase = true))
        assertTrue(exception.bodyPreview.contains("Internal Server Error"))
    }

    @Test
    fun `200 non-json content type throws GeApiHttpException`() {
        val client =
            geClientResponding(
                status = HttpStatusCode.OK,
                body = "<html>login page</html>",
                contentType = "text/html",
            )

        val exception =
            assertThrows(GeApiHttpException::class.java) {
                runBlocking { client.get<SamplePayload>("https://wrong.example/") }
            }

        assertEquals(200, exception.statusCode)
        assertTrue(exception.bodyPreview.contains("login page"))
        assertTrue(exception.message!!.contains("JSON", ignoreCase = true))
    }

    @Test
    fun `200 json string body throws GeApiHttpException instead of JsonConvertException`() {
        val client =
            geClientResponding(
                status = HttpStatusCode.OK,
                body = "\"not-an-object\"",
                contentType = "application/json",
            )

        val exception =
            assertThrows(GeApiHttpException::class.java) {
                runBlocking { client.get<SamplePayload>("https://ge.example/api/builds") }
            }

        assertEquals(200, exception.statusCode)
        assertTrue(exception.bodyPreview.contains("not-an-object"))
    }

    @Test
    fun `successful json object is deserialized`() {
        val client =
            geClientResponding(
                status = HttpStatusCode.OK,
                body = """{"id":"scan-1","ok":true}""",
                contentType = "application/json",
            )

        val result = runBlocking { client.get<SamplePayload>("https://ge.example/api/builds/scan-1") }

        assertEquals("scan-1", result.id)
        assertEquals(true, result.ok)
    }

    @Test
    fun `redacts sensitive query parameters in exception url`() {
        val redacted =
            GeApiResponseValidation.redactUrl(
                "https://ge.example/api/builds?apiKey=super-secret&maxBuilds=10",
            )

        assertTrue(redacted.contains("apiKey=REDACTED"))
        assertTrue(redacted.contains("maxBuilds=10"))
        assertTrue(!redacted.contains("super-secret"))
    }

    private fun geClientResponding(
        status: HttpStatusCode,
        body: String,
        contentType: String,
    ): GEClient {
        val engine =
            MockEngine {
                respond(
                    content = body,
                    status = status,
                    headers = headersOf(HttpHeaders.ContentType, contentType),
                )
            }
        val httpClient =
            HttpClient(engine) {
                expectSuccess = false
            }
        return GEClient(token = "test-token", geServer = "https://ge.example", httpClient = httpClient)
    }

    data class SamplePayload(
        val id: String,
        val ok: Boolean,
    )
}
