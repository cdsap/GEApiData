package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.model.CustomValue
import io.github.cdsap.geapi.client.model.Environment
import io.github.cdsap.geapi.client.model.GradleScan
import io.github.cdsap.geapi.client.repository.BuildScanAttributesRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetSingleBuildScanAttributesRequestTest {
    @Test
    fun acceptsRepositoryWithOnlyBuildScanAttributeOperations() =
        runBlocking {
            val request = GetSingleBuildScanAttributesRequest(AttributeRepository())

            val result = request.get("build-1")

            assertEquals("build-1", result.id)
            assertEquals("gradle", result.buildTool)
        }
}

private class AttributeRepository : BuildScanAttributesRepository {
    override suspend fun getBuildScanGradleAttribute(id: String): GradleScan =
        GradleScan(
            id = id,
            rootProjectName = "project",
            requestedTasks = arrayOf("test"),
            tags = emptyArray(),
            hasFailed = false,
            environment = Environment(username = "user", numberOfCpuCores = "2"),
            buildDuration = 100,
            buildStartTime = 1,
            values = arrayOf(CustomValue("key", "value")),
        )

    override suspend fun getBuildScanMavenAttribute(id: String) =
        error("Maven attributes are not used by this request")
}
