package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.model.ArtifactTransform
import io.github.cdsap.geapi.client.model.ArtifactTransforms
import io.github.cdsap.geapi.client.model.ChangedAttributes
import io.github.cdsap.geapi.client.network.GeApiHttpException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class GetSingleArtifactTransformRequestTest {
    private val repository = FakeArtifactRepository()
    private val getSingleArtifactTransformRequest = GetSingleBuildArtifactTransformRequest(repository)

    @Test
    fun `returns empty list when no builds are provided`() =
        runBlocking {
            val result = getSingleArtifactTransformRequest.get("null")

            assertEquals(emptyList<ArtifactTransform>(), result)
        }

    @Test
    fun `returns artifact transforms for provided builds`() =
        runBlocking {
            val result = getSingleArtifactTransformRequest.get("2")

            assertEquals(repository.artifactTransforms.toList(), result)
        }

    @Test
    fun `does not swallow GeApiHttpException as empty list`() {
        val failing =
            GetSingleBuildArtifactTransformRequest(
                object : FakeTestRepository() {
                    override suspend fun getArtifactTransformRequest(id: String): ArtifactTransforms =
                        throw GeApiHttpException(
                            statusCode = 401,
                            requestUrl = "https://ge.example/api/builds/$id/artifact-transformers",
                            bodyPreview = "Unauthorized",
                        )
                },
            )

        val exception =
            assertThrows(GeApiHttpException::class.java) {
                runBlocking { failing.get("scan-1") }
            }

        assertEquals(401, exception.statusCode)
    }
}

internal class FakeArtifactRepository : FakeTestRepository() {
    val artifactTransforms =
        arrayOf(
            ArtifactTransform(
                artifactTransformExecutionName = "myTransform",
                transformActionType = "dex",
                inputArtifactName = "classes.jar",
                outcome = "from_cache",
                avoidanceOutcome = "avoided_from_cache",
                duration = "12",
                fingerprintingDuration = "12",
                cacheArtifactSize = "0",
                changedAttributes =
                    arrayOf(
                        ChangedAttributes("f", "api", "om"),
                    ),
            ),
            ArtifactTransform(
                artifactTransformExecutionName = "myTransform",
                transformActionType = "dex",
                inputArtifactName = "classes.jar",
                outcome = "from_cache",
                avoidanceOutcome = "avoided_from_cache",
                duration = "12",
                fingerprintingDuration = "12",
                cacheArtifactSize = "0",
                changedAttributes =
                    arrayOf(
                        ChangedAttributes("f", "api", "om"),
                    ),
            ),
        )

    override suspend fun getArtifactTransformRequest(id: String): ArtifactTransforms {
        if (id == "null") {
            return ArtifactTransforms(emptyArray())
        }

        return ArtifactTransforms(artifactTransforms)
    }
}
