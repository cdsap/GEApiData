package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.model.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetUsageResourcesTest {
    @Test
    fun givenGradleBuildItReturnsProcessUsage() =
        runBlocking {
            val resourceUsage = GetBuildsResourceUsageRequest(FakeUsageRepository(false))

            val filter =
                Filter(
                    maxBuilds = 100,
                    concurrentCalls = 1,
                    includeFailedBuilds = false,
                    project = "nowinandroid",
                    tags = listOf("tag3"),
                    requestedTask = null,
                    user = null,
                )

            val result =
                resourceUsage.get(
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
                    ),
                    filter,
                )

            assertEquals(1, result.size)
            assertEquals(result[0].builtTool, "gradle")
            assert(result[0].tags.contains("tag3"))
            assertEquals(result[0].total.allProcessesCpu.max == 100L, true)
            assertEquals(result[0].total.allProcessesCpu.average == 50L, true)
            assertEquals(result[0].total.allProcessesCpu.median == 51L, true)
            assertEquals(result[0].total.allProcessesCpu.p25 == 25L, true)
        }

    @Test
    fun givenMavenBuildItDoesNotReturnProcessUsage() =
        runBlocking {
            val resourceUsage = GetBuildsResourceUsageRequest(FakeUsageRepository(true))

            val filter =
                Filter(
                    maxBuilds = 100,
                    concurrentCalls = 1,
                    includeFailedBuilds = false,
                    project = "nowinandroid",
                    tags = listOf("tag3"),
                    requestedTask = null,
                    user = null,
                )

            val result =
                resourceUsage.get(
                    listOf(
                        ScanWithAttributes(
                            id = "2",
                            projectName = "AnotherProject",
                            requestedTasksGoals = arrayOf("test"),
                            tags = arrayOf("tag3"),
                            hasFailed = true,
                            environment = Environment(username = "user2", numberOfCpuCores = "3"),
                            buildDuration = 1500,
                            buildTool = "maven",
                            buildStartTime = 1789,
                            values = arrayOf(CustomValue("a", "b")),
                        ),
                    ),
                    filter,
                )

            assertEquals(0, result.size)
        }
}

internal class FakeUsageRepository(val maven: Boolean) : FakeTestRepository() {
    val metric =
        Metric(
            average = 50L,
            median = 51L,
            max = 100L,
            p25 = 25L,
            p75 = 75L,
            p95 = 95L,
        )

    val performanceMetrics =
        PerformanceMetrics(
            buildProcessCpu = metric,
            allProcessesCpu = metric,
            allProcessesMemory = metric,
            buildChildProcessesCpu = metric,
            buildChildProcessesMemory = metric,
            buildProcessMemory = metric,
            diskReadThroughput = metric,
            diskWriteThroughput = metric,
            networkUploadThroughput = metric,
            networkDownloadThroughput = metric,
        )

    override suspend fun getBuildScanGradlePerformance(id: String): PerformanceUsage {
        return PerformanceUsage(
            builtTool = if (maven) "maven" else "gradle",
            environment = Environment(username = "user2", numberOfCpuCores = "3"),
            totalMemory = 1000L,
            total = performanceMetrics,
            execution = performanceMetrics,
            nonExecution = performanceMetrics,
        )
    }
}
