package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.model.ArtifactTransform
import io.github.cdsap.geapi.client.model.ArtifactTransforms
import io.github.cdsap.geapi.client.model.ChangedAttributes
import io.github.cdsap.geapi.client.model.CustomValue
import io.github.cdsap.geapi.client.model.Environment
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.model.ScanWithAttributes
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.seconds

class GetBuildsWithArtifactTransformRequestTest {
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

    @Test
    fun `uses withPermit instead of manual semaphore acquire and release`() {
        val source =
            File("src/main/kotlin/io/github/cdsap/geapi/client/domain/impl/GetBuildsWithArtifactTransformRequest.kt")
                .readText()

        assertTrue(source.contains("withPermit"))
        assertFalse(source.contains("semaphore.acquire()"))
        assertFalse(source.contains("semaphore.release()"))
    }

    @Test
    fun `returns empty list when repository throws NullPointerException`() =
        runBlocking {
            val failingRepository =
                object : FakeTestRepository() {
                    override suspend fun getArtifactTransformRequest(id: String): ArtifactTransforms {
                        throw NullPointerException()
                    }
                }
            val request = GetBuildsWithArtifactTransformRequest(failingRepository)
            val builds =
                listOf(
                    ScanWithAttributes(
                        id = "1",
                        projectName = "project",
                        requestedTasksGoals = arrayOf("test"),
                        tags = arrayOf("tag"),
                        hasFailed = false,
                        environment = Environment(username = "user", numberOfCpuCores = "2"),
                        buildDuration = 100,
                        buildTool = "gradle",
                        buildStartTime = 1,
                        values = arrayOf(CustomValue("k", "v")),
                    ),
                )

            val result =
                withTimeout(5.seconds) {
                    request.get(builds, Filter(concurrentCallsConservative = 1))
                }

            assertEquals(emptyList<ArtifactTransform>(), result)
        }

    @Test
    fun `propagates repository failures without hanging`() =
        runBlocking {
            val failingRepository =
                object : FakeTestRepository() {
                    override suspend fun getArtifactTransformRequest(id: String): ArtifactTransforms {
                        throw RuntimeException("repository failure")
                    }
                }
            val request = GetBuildsWithArtifactTransformRequest(failingRepository)
            val builds =
                listOf(
                    ScanWithAttributes(
                        id = "1",
                        projectName = "project",
                        requestedTasksGoals = arrayOf("test"),
                        tags = arrayOf("tag"),
                        hasFailed = false,
                        environment = Environment(username = "user", numberOfCpuCores = "2"),
                        buildDuration = 100,
                        buildTool = "gradle",
                        buildStartTime = 1,
                        values = arrayOf(CustomValue("k", "v")),
                    ),
                    ScanWithAttributes(
                        id = "2",
                        projectName = "project",
                        requestedTasksGoals = arrayOf("test"),
                        tags = arrayOf("tag"),
                        hasFailed = false,
                        environment = Environment(username = "user", numberOfCpuCores = "2"),
                        buildDuration = 100,
                        buildTool = "gradle",
                        buildStartTime = 1,
                        values = arrayOf(CustomValue("k", "v")),
                    ),
                )

            val thrown =
                assertFailsWith<RuntimeException> {
                    withTimeout(5.seconds) {
                        request.get(builds, Filter(concurrentCallsConservative = 1))
                    }
                }

            assertEquals("repository failure", thrown.message)
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
