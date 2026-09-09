package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.domain.impl.logger.Logger
import io.github.cdsap.geapi.client.model.AvoidanceSavingsSummary
import io.github.cdsap.geapi.client.model.Build
import io.github.cdsap.geapi.client.model.BuildProfileOverview
import io.github.cdsap.geapi.client.model.BuildWithResourceUsage
import io.github.cdsap.geapi.client.model.ConfigurationCacheResult
import io.github.cdsap.geapi.client.model.CustomValue
import io.github.cdsap.geapi.client.model.Environment
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.model.GradleScan
import io.github.cdsap.geapi.client.model.Scan
import io.github.cdsap.geapi.client.model.ScanWithAttributes
import io.github.cdsap.geapi.client.model.ArtifactTransform
import io.github.cdsap.geapi.client.model.ArtifactTransforms
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.seconds

class ConcurrentSemaphorePermitReleaseTest {
    @Test
    fun listedRequestLoopsUseWithPermitInsteadOfManualRelease() {
        val requestSources =
            listOf(
                "GetBuildsWithArtifactTransformRequest.kt",
                "GetBuildsWithCachePerformanceRequest.kt",
                "GetBuildsResourceUsageRequest.kt",
                "GetScanAttribute.kt",
                "BuildScanBatchProcessor.kt",
            )

        requestSources.forEach { fileName ->
            val source = File("src/main/kotlin/io/github/cdsap/geapi/client/domain/impl/$fileName").readText()
            assertTrue(source.contains("withPermit"), "$fileName should use Semaphore.withPermit")
            assertFalse(source.contains("semaphore.acquire()"), "$fileName should not call acquire() manually")
            assertFalse(source.contains("semaphore.release()"), "$fileName should not call release() manually")
        }
    }

    @Test
    fun convertedBatchRequestsDelegateSemaphoreAndProgressToHelper() {
        val convertedSources =
            listOf(
                "GetBuildsProfileRequest.kt",
                "GetConfigurationCacheResultRequest.kt",
            )

        convertedSources.forEach { fileName ->
            val source = File("src/main/kotlin/io/github/cdsap/geapi/client/domain/impl/$fileName").readText()
            assertTrue(source.contains("BuildScanBatchProcessor"), "$fileName should use BuildScanBatchProcessor")
            assertFalse(source.contains("Semaphore("), "$fileName should not allocate Semaphore directly")
            assertFalse(source.contains("ProgressFeedback("), "$fileName should not allocate ProgressFeedback directly")
            assertFalse(source.contains("withPermit"), "$fileName should not own semaphore permits")
        }
    }

    @Test
    fun cachePerformancePropagatesRepositoryFailureWithoutHanging() =
        runBlocking {
            val request = GetBuildsWithCachePerformanceRequest(FailingCacheRepository())
            val filter = Filter(concurrentCallsConservative = 1)

            val thrown =
                assertFailsWith<RuntimeException> {
                    withTimeout(5.seconds) {
                        request.get(sampleScans(3), filter)
                    }
                }

            assertEquals("repository failure", thrown.message)
        }

    @Test
    fun resourceUsagePropagatesRepositoryFailureWithoutHanging() =
        runBlocking {
            val request = GetBuildsResourceUsageRequest(FailingUsageRepository())
            val filter = Filter(concurrentCallsConservative = 1)

            val thrown =
                assertFailsWith<RuntimeException> {
                    withTimeout(5.seconds) {
                        request.get(sampleScans(3), filter)
                    }
                }

            assertEquals("repository failure", thrown.message)
        }

    @Test
    fun profilePropagatesRepositoryFailureWithoutHanging() =
        runBlocking {
            val request = GetBuildsProfileRequest(FailingProfileRepository())
            val filter = Filter(concurrentCallsConservative = 1)

            val thrown =
                assertFailsWith<RuntimeException> {
                    withTimeout(5.seconds) {
                        request.get(sampleScans(3), filter)
                    }
                }

            assertEquals("repository failure", thrown.message)
        }

    @Test
    fun configurationCachePropagatesRepositoryFailureWithoutHanging() =
        runBlocking {
            val request = GetConfigurationCacheResultRequest(FailingConfigurationCacheRepository())
            val filter = Filter(concurrentCallsConservative = 1)

            val thrown =
                assertFailsWith<RuntimeException> {
                    withTimeout(5.seconds) {
                        request.get(sampleScans(3), filter)
                    }
                }

            assertEquals("repository failure", thrown.message)
        }

    @Test
    fun scanAttributePropagatesRepositoryFailureWithoutHanging() =
        runBlocking {
            val request = GetScanAttribute(FailingScanAttributeRepository())
            val filter = Filter(concurrentCalls = 1)
            val scans =
                listOf(
                    Scan(id = "a", buildToolType = "gradle"),
                    Scan(id = "b", buildToolType = "gradle"),
                    Scan(id = "c", buildToolType = "gradle"),
                )

            val thrown =
                assertFailsWith<RuntimeException> {
                    withTimeout(5.seconds) {
                        request.getScanAttributes(scans, filter, Logger(filter.clientType))
                    }
                }

            assertEquals("repository failure", thrown.message)
        }

    @Test
    fun artifactTransformPropagatesRepositoryFailureWithoutHanging() =
        runBlocking {
            val request = GetBuildsWithArtifactTransformRequest(FailingArtifactTransformRepository())
            val filter = Filter(concurrentCallsConservative = 1)

            val thrown =
                assertFailsWith<RuntimeException> {
                    withTimeout(5.seconds) {
                        request.get(sampleScans(3), filter)
                    }
                }

            assertEquals("repository failure", thrown.message)
        }

    @Test
    fun artifactTransformReturnsEmptyForNullPointerException() =
        runBlocking {
            val request = GetBuildsWithArtifactTransformRequest(NullPointerArtifactTransformRepository())
            val filter = Filter(concurrentCallsConservative = 1)

            val result =
                withTimeout(5.seconds) {
                    request.get(sampleScans(2), filter)
                }

            assertEquals(emptyList<ArtifactTransform>(), result)
        }

    @Test
    fun cachePerformanceCompletesAllBuildsWhenConcurrencyIsOne() =
        runBlocking {
            val request = GetBuildsWithCachePerformanceRequest(SucceedingCacheRepository())
            val filter = Filter(concurrentCallsConservative = 1)

            val result =
                withTimeout(5.seconds) {
                    request.get(sampleScans(3), filter)
                }

            assertEquals(3, result.size)
        }

    private fun sampleScans(count: Int): List<ScanWithAttributes> {
        return (1..count).map { index ->
            ScanWithAttributes(
                id = "scan-$index",
                projectName = "project",
                requestedTasksGoals = arrayOf("test"),
                tags = arrayOf("tag"),
                hasFailed = false,
                environment = Environment(username = "user", numberOfCpuCores = "2"),
                buildDuration = 100,
                buildTool = "gradle",
                buildStartTime = 1,
                values = arrayOf(CustomValue("k", "v")),
            )
        }
    }
}

private fun sampleBuild(id: String): Build {
    return Build(
        taskExecution = emptyArray(),
        id = id,
        buildDuration = 1,
        avoidanceSavingsSummary = AvoidanceSavingsSummary("0", "0", "0"),
        builtTool = "gradle",
        goalExecution = emptyArray(),
    )
}

private class FailingCacheRepository : FakeTestRepository() {
    override suspend fun getBuildScanGradleCachePerformance(id: String): Build {
        throw RuntimeException("repository failure")
    }
}

private class SucceedingCacheRepository : FakeTestRepository() {
    override suspend fun getBuildScanGradleCachePerformance(id: String): Build = sampleBuild(id)
}

private class FailingUsageRepository : FakeTestRepository() {
    override suspend fun getBuildScanGradlePerformance(id: String): BuildWithResourceUsage {
        throw RuntimeException("repository failure")
    }
}

private class FailingProfileRepository : FakeTestRepository() {
    override suspend fun getBuildProfileOverview(id: String): BuildProfileOverview {
        throw RuntimeException("repository failure")
    }
}

private class FailingConfigurationCacheRepository : FakeTestRepository() {
    override suspend fun getConfigurationCacheResult(id: String): ConfigurationCacheResult {
        throw RuntimeException("repository failure")
    }
}

private class FailingScanAttributeRepository : FakeTestRepository() {
    override suspend fun getBuildScanGradleAttribute(id: String): GradleScan {
        throw RuntimeException("repository failure")
    }
}

private class FailingArtifactTransformRepository : FakeTestRepository() {
    override suspend fun getArtifactTransformRequest(id: String): ArtifactTransforms {
        throw RuntimeException("repository failure")
    }
}

private class NullPointerArtifactTransformRepository : FakeTestRepository() {
    override suspend fun getArtifactTransformRequest(id: String): ArtifactTransforms {
        throw NullPointerException()
    }
}
