package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.model.ArtifactTransform
import io.github.cdsap.geapi.client.model.ArtifactTransforms
import io.github.cdsap.geapi.client.model.Build
import io.github.cdsap.geapi.client.model.ChangedAttributes
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.model.GradleScan
import io.github.cdsap.geapi.client.model.MavenScan
import io.github.cdsap.geapi.client.model.Scan
import io.github.cdsap.geapi.client.repository.GradleEnterpriseRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetSingleArtifactTransformRequestTest {

    private val repository = FakeArtifactRepository()
    private val getSingleArtifactTransformRequest = GetSingleBuildArtifactTransformRequest(repository)

    @Test
    fun `returns empty list when no builds are provided`() = runBlocking {
        val result = getSingleArtifactTransformRequest.get("null")

        assertEquals(emptyList<ArtifactTransform>(), result)
    }

    @Test
    fun `returns artifact transforms for provided builds`() = runBlocking {
        val result = getSingleArtifactTransformRequest.get("2")

        assertEquals(repository.artifactTransforms.toList(), result)
    }
}

internal class FakeArtifactRepository : GradleEnterpriseRepository {

    val artifactTransforms = arrayOf(
        ArtifactTransform(
            artifactTransformExecutionName = "myTransform",
            transformActionType = "dex",
            inputArtifactName = "classes.jar",
            outcome = "from_cache",
            avoidanceOutcome = "avoided_from_cache",
            duration = "12",
            fingerprintingDuration = "12",
            cacheArtifactSize = "0",
            changedAttributes = arrayOf(
                ChangedAttributes("f", "api", "om")
            )
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
            changedAttributes = arrayOf(
                ChangedAttributes("f", "api", "om")
            )
        )
    )
    override suspend fun getBuildScans(filter: Filter, buildId: String?): Array<Scan> {
        TODO("Not yet implemented")
    }

    override suspend fun getBuildScansWithAdvancedQuery(filter: Filter, buildId: String?): Array<Scan> {
        TODO("Not yet implemented")
    }

    override suspend fun getBuildScanGradleAttribute(id: String): GradleScan {
        TODO("Not yet implemented")
    }

    override suspend fun getBuildScanMavenAttribute(id: String): MavenScan {
        TODO("Not yet implemented")
    }

    override suspend fun getBuildScanGradleCachePerformance(id: String): Build {
        TODO("Not yet implemented")
    }

    override suspend fun getBuildScanMavenCachePerformance(id: String): Build {
        TODO("Not yet implemented")
    }

    override suspend fun getArtifactTransformRequest(id: String): ArtifactTransforms {
        if (id == "null") {
            return ArtifactTransforms(emptyArray())
        }

        return ArtifactTransforms(artifactTransforms)
    }
}
