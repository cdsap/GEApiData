package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.model.ConfigurationCacheOperation
import io.github.cdsap.geapi.client.model.ConfigurationCacheResult
import io.github.cdsap.geapi.client.model.ConfigurationCacheResultResponse
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

class GetConfigurationCacheResultRequestTest {
    @Test
    fun delegatesBoundedProgressExecutionToBatchProcessor() {
        val requestSource =
            File("src/main/kotlin/io/github/cdsap/geapi/client/domain/impl/GetConfigurationCacheResultRequest.kt")
                .readText()
        val helperSource =
            File("src/main/kotlin/io/github/cdsap/geapi/client/domain/impl/BuildScanBatchProcessor.kt")
                .readText()

        assertTrue(requestSource.contains("BuildScanBatchProcessor"))
        assertFalse(requestSource.contains("Semaphore("))
        assertFalse(requestSource.contains("ProgressFeedback("))
        assertTrue(helperSource.contains("withPermit"))
        assertFalse(helperSource.contains("semaphore.acquire()"))
        assertFalse(helperSource.contains("semaphore.release()"))
    }

    @Test
    fun returnsEmptyListWhenNoBuildsAreProvided() =
        runBlocking {
            val request = GetConfigurationCacheResultRequest(SucceedingConfigurationCacheRepository())

            val result = request.get(emptyList(), Filter())

            assertEquals(emptyList<ConfigurationCacheResult>(), result)
        }

    @Test
    fun returnsResultsForGradleBuildsInInputOrderAfterFiltering() =
        runBlocking {
            val request = GetConfigurationCacheResultRequest(SucceedingConfigurationCacheRepository())
            val builds =
                listOf(
                    sampleScan(id = "gradle-1", buildTool = "gradle"),
                    sampleScan(id = "maven-1", buildTool = "maven"),
                    sampleScan(id = "gradle-2", buildTool = "gradle"),
                    sampleScan(id = "gradle-3", buildTool = "gradle"),
                )

            val result = request.get(builds, Filter(concurrentCallsConservative = 1))

            assertEquals(3, result.size)
            assertEquals(
                listOf("gradle-1", "gradle-2", "gradle-3"),
                result.map { it.result.outcome },
            )
        }

    @Test
    fun propagatesRepositoryFailureWithoutDependingOnHappyPathPermitRelease() =
        runBlocking {
            val request = GetConfigurationCacheResultRequest(FailingConfigurationCacheOverviewRepository())
            val filter = Filter(concurrentCallsConservative = 1)

            val thrown =
                assertFailsWith<RuntimeException> {
                    withTimeout(5.seconds) {
                        request.get(
                            listOf(
                                sampleScan(id = "scan-1", buildTool = "gradle"),
                                sampleScan(id = "scan-2", buildTool = "gradle"),
                                sampleScan(id = "scan-3", buildTool = "gradle"),
                            ),
                            filter,
                        )
                    }
                }

            assertEquals("repository failure", thrown.message)
        }

    private fun sampleScan(
        id: String,
        buildTool: String,
    ): ScanWithAttributes {
        return ScanWithAttributes(
            id = id,
            projectName = "project",
            requestedTasksGoals = arrayOf("test"),
            tags = arrayOf("tag"),
            hasFailed = false,
            environment = Environment(username = "user", numberOfCpuCores = "2"),
            buildDuration = 100,
            buildTool = buildTool,
            buildStartTime = 1,
            values = arrayOf(CustomValue("k", "v")),
        )
    }
}

private fun sampleConfigurationCacheResult(outcome: String): ConfigurationCacheResult {
    return ConfigurationCacheResult(
        result =
            ConfigurationCacheResultResponse(
                outcome = outcome,
                entrySize = 1L,
                store = ConfigurationCacheOperation(duration = 1L, hasFailed = false),
                load = null,
            ),
    )
}

private class SucceedingConfigurationCacheRepository : FakeTestRepository() {
    override suspend fun getConfigurationCacheResult(id: String): ConfigurationCacheResult {
        return sampleConfigurationCacheResult(id)
    }
}

private class FailingConfigurationCacheOverviewRepository : FakeTestRepository() {
    override suspend fun getConfigurationCacheResult(id: String): ConfigurationCacheResult {
        throw RuntimeException("repository failure")
    }
}
