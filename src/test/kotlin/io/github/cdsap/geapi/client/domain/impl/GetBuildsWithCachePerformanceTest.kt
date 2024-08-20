package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.model.AvoidanceSavingsSummary
import io.github.cdsap.geapi.client.model.Build
import io.github.cdsap.geapi.client.model.CustomValue
import io.github.cdsap.geapi.client.model.Environment
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.model.Goal
import io.github.cdsap.geapi.client.model.ScanWithAttributes
import io.github.cdsap.geapi.client.model.Task
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetCachePerformanceImplTest {
    @Test
    fun givenGradleBuildsAllTheBuildScansAreReturnedAndAttributesAreAggregated() =
        runBlocking {
            val getCachePerformance = GetBuildsWithCachePerformanceRequest(FakeCacheGradleEnterpriseRepository())

            val filter =
                Filter(
                    maxBuilds = 100,
                    concurrentCalls = 1,
                    includeFailedBuilds = false,
                    project = "nowinandroid",
                    tags = listOf("tag1", "tag2"),
                    requestedTask = null,
                    user = null,
                )

            val result =
                getCachePerformance.get(
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
            assertEquals(result[0].buildStartTime, 1789)
            assert(result[0].tags.contains("tag3"))
            assertEquals(result[0].projectName, "AnotherProject")
            assert(result[0].requestedTask.contains("test"))
            assertEquals(result[0].taskExecution.size, 2)
            assertEquals(result[0].values[0].name, "a")
            assertEquals(result[0].values[0].value, "b")
        }

    @Test
    fun giveMavenBuildsAllTheBuildScansAreReturnedAndAttributesAreAggregated() =
        runBlocking {
            val getCachePerformance = GetBuildsWithCachePerformanceRequest(FakeCacheGradleEnterpriseRepository())

            val filter =
                Filter(
                    maxBuilds = 100,
                    concurrentCalls = 1,
                    includeFailedBuilds = false,
                    project = "nowinandroid",
                    tags = listOf("tag1", "tag2"),
                    requestedTask = null,
                    user = null,
                )

            val result =
                getCachePerformance.get(
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

            assertEquals(1, result.size)
            assertEquals(result[0].builtTool, "maven")
            assertEquals(result[0].buildStartTime, 1789)
            assert(result[0].tags.contains("tag3"))
            assertEquals(result[0].projectName, "AnotherProject")
            assertEquals(result[0].projectName, "AnotherProject")
            assert(result[0].requestedTask.contains("test"))
            assertEquals(result[0].values[0].name, "a")

            assertEquals(result[0].values[0].value, "b")
        }
}

internal class FakeCacheGradleEnterpriseRepository : FakeTestRepository() {
    override suspend fun getBuildScanGradleCachePerformance(id: String): Build {
        return Build(
            taskExecution =
                arrayOf(
                    Task(
                        "type1",
                        ":type1",
                        "from-cache",
                        12,
                        12,
                    ),
                    Task(
                        "type2",
                        ":type2",
                        "from-cache",
                        12,
                        12,
                    ),
                ),
            id = "2332",
            buildDuration = 24,
            avoidanceSavingsSummary = AvoidanceSavingsSummary("12", "1", "1"),
            builtTool = "gradle",
            goalExecution = emptyArray(),
        )
    }

    override suspend fun getBuildScanMavenCachePerformance(id: String): Build {
        return Build(
            taskExecution = emptyArray(),
            id = "2332",
            buildDuration = 24,
            avoidanceSavingsSummary = AvoidanceSavingsSummary("12", "1", "1"),
            builtTool = "maven",
            goalExecution =
                arrayOf(
                    Goal(
                        goalName = "goalName",
                        mojoType = "mojoType",
                        goalExecutionId = "goalExecutionId",
                        goalProjectName = "goalProjectName",
                        avoidanceOutcome = "executed",
                        nonCacheabilityCategory = "nonCacheabilityCategory",
                        nonCacheabilityReason = "nonCacheabilityReason",
                        fingerprintingDuration = 10,
                        duration = 10,
                    ),
                ),
        )
    }
}
