package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.model.ArtifactTransform
import io.github.cdsap.geapi.client.model.ArtifactTransforms
import io.github.cdsap.geapi.client.model.ChangedAttributes
import io.github.cdsap.geapi.client.model.CustomValue
import io.github.cdsap.geapi.client.model.Environment
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.model.ScanWithAttributes
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetConfigurationCacheResultRequestTest {
    private val repository = FakeRepository()
    private val getBuildsWithArtifactTransformRequest = GetBuildsWithArtifactTransformRequest(repository)

    @Test
    fun `returns empty list when no builds are provided`() =
        runBlocking {
            val result = getBuildsWithArtifactTransformRequest.get(emptyList(), Filter())

            assertEquals(emptyList<ArtifactTransform>(), result)
        }

    @Test
    fun `returns artifact transforms for provided builds`() =
        runBlocking {
            val builds =
                listOf(
                    ScanWithAttributes(
                        id = "2",
                        projectName = "AnotherProject",
                        requestedTasksGoals = arrayOf("test"),
                        tags = arrayOf("tag3"),
                        hasFailed = true,
                        environment = Environment(username = "user2", numberOfCpuCores = "3"),
                        buildDuration = 1500,
                        buildTool = "gradle",
                        buildStartTime = 1789,
                        values = arrayOf(CustomValue("a", "b")),
                    ),
                    ScanWithAttributes(
                        id = "3",
                        projectName = "AnotherProject",
                        requestedTasksGoals = arrayOf("test"),
                        tags = arrayOf("tag3"),
                        hasFailed = true,
                        environment = Environment(username = "user2", numberOfCpuCores = "3"),
                        buildDuration = 1500,
                        buildTool = "gradle",
                        buildStartTime = 1789,
                        values = arrayOf(CustomValue("a", "b")),
                    ),
                )

            val result = getBuildsWithArtifactTransformRequest.get(builds, Filter())

            assertEquals(repository.artifactTransforms.toList() + repository.artifactTransforms.toList(), result)
        }
}

internal class FakeRepository : FakeTestRepository() {
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
                avoidanceSavings = "0",
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
                avoidanceSavings = "0",
                fingerprintingDuration = "12",
                cacheArtifactSize = "0",
                changedAttributes =
                    arrayOf(
                        ChangedAttributes("f", "api", "om"),
                    ),
            ),
        )

    override suspend fun getArtifactTransformRequest(id: String): ArtifactTransforms {
        return ArtifactTransforms(artifactTransforms)
    }
}
