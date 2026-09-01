package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.model.Breakdown
import io.github.cdsap.geapi.client.model.BuildProfileOverview
import io.github.cdsap.geapi.client.model.CustomValue
import io.github.cdsap.geapi.client.model.Environment
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.model.MemoryPool
import io.github.cdsap.geapi.client.model.MemoryUsage
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

class GetBuildsProfileRequestTest {
    @Test
    fun usesWithPermitInsteadOfManualAcquireRelease() {
        val source =
            File("src/main/kotlin/io/github/cdsap/geapi/client/domain/impl/GetBuildsProfileRequest.kt")
                .readText()

        assertTrue(source.contains("withPermit"))
        assertFalse(source.contains("semaphore.acquire()"))
        assertFalse(source.contains("semaphore.release()"))
    }

    @Test
    fun returnsEmptyListWhenNoBuildsAreProvided() =
        runBlocking {
            val request = GetBuildsProfileRequest(SucceedingProfileRepository())

            val result = request.get(emptyList(), Filter())

            assertEquals(emptyList<BuildProfileOverview>(), result)
        }

    @Test
    fun returnsProfilesForGradleBuildsAndFiltersOtherBuildTools() =
        runBlocking {
            val request = GetBuildsProfileRequest(SucceedingProfileRepository())
            val builds =
                listOf(
                    sampleScan(id = "gradle-1", buildTool = "gradle"),
                    sampleScan(id = "maven-1", buildTool = "maven"),
                    sampleScan(id = "gradle-2", buildTool = "gradle"),
                )

            val result = request.get(builds, Filter(concurrentCallsConservative = 1))

            assertEquals(2, result.size)
            assertEquals(listOf("gradle-1", "gradle-2"), result.map { it.id })
        }

    @Test
    fun propagatesRepositoryFailureWithoutDependingOnHappyPathPermitRelease() =
        runBlocking {
            val request = GetBuildsProfileRequest(FailingProfileOverviewRepository())
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

private fun sampleProfileOverview(): BuildProfileOverview {
    return BuildProfileOverview(
        breakdown =
            Breakdown(
                total = 100,
                initialization = 10,
                configuration = 20,
                execution = 60,
                endOfBuild = 10,
            ),
        memoryUsage =
            MemoryUsage(
                totalGarbageCollectionTime = 5,
                memoryPools =
                    arrayOf(
                        MemoryPool(name = "heap", peakMemory = 1000, maxMemory = 2000),
                    ),
            ),
    )
}

private class SucceedingProfileRepository : FakeTestRepository() {
    override suspend fun getBuildProfileOverview(id: String): BuildProfileOverview {
        return sampleProfileOverview()
    }
}

private class FailingProfileOverviewRepository : FakeTestRepository() {
    override suspend fun getBuildProfileOverview(id: String): BuildProfileOverview {
        throw RuntimeException("repository failure")
    }
}
