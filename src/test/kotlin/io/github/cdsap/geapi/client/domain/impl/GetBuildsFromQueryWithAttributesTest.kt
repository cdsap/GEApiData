package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.model.ArtifactTransforms
import io.github.cdsap.geapi.client.model.Build
import io.github.cdsap.geapi.client.model.BuildWithResourceUsage
import io.github.cdsap.geapi.client.model.ConfigurationCacheResult
import io.github.cdsap.geapi.client.model.Environment
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.model.GradleScan
import io.github.cdsap.geapi.client.model.MavenScan
import io.github.cdsap.geapi.client.model.Scan
import io.github.cdsap.geapi.client.repository.GradleEnterpriseRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetBuildsFromQueryWithAttributesTest {
    @Test
    fun givenGradleBuildsAllTheBuildScansAreReturned() =
        runBlocking {
            val getBuildScansWithQuery =
                GetBuildsFromQueryWithAttributesRequest(FakeGradleEnterpriseRepositoryWithQuery(listOf("gradle")))

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

            val result = getBuildScansWithQuery.get(filter)

            assertEquals(100, result.size)
        }

    @Test
    fun givenEmptyBuildsResultsAreEmpty() =
        runBlocking {
            val getBuildScansWithQuery =
                GetBuildsFromQueryWithAttributesRequest(FakeEmptyRepositoryWithQuery())

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

            val result = getBuildScansWithQuery.get(filter)

            assertEquals(0, result.size)
        }

    @Test
    fun givenGradleAndMavenBuildsAllTheBuildScansAreReturned() =
        runBlocking {
            val getBuildScansWithQuery =
                GetBuildsFromQueryWithAttributesRequest(FakeGradleEnterpriseRepositoryWithQuery(listOf("gradle", "maven")))

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

            val result = getBuildScansWithQuery.get(filter)

            assertEquals(100, result.size)
        }

    @Test
    fun givenGradleBazelAndMavenBazelBuildsAreNotReturned() =
        runBlocking {
            val getBuildScansWithQuery =
                GetBuildsFromQueryWithAttributesRequest(
                    FakeGradleEnterpriseRepositoryWithQuery(
                        listOf(
                            "gradle",
                            "maven",
                            "bazel",
                        ),
                    ),
                )

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

            val result = getBuildScansWithQuery.get(filter)

            assertEquals(90, result.size)
        }
}

internal class FakeGradleEnterpriseRepositoryWithQuery(private val buildSystems: List<String>) :
    GradleEnterpriseRepository {
    override suspend fun getBuildScans(
        filter: Filter,
        buildId: String?,
    ): Array<Scan> {
        TODO("Not yet implemented")
    }

    override suspend fun getBuildScansWithAdvancedQuery(
        filter: Filter,
        buildId: String?,
    ): Array<Scan> {
        val scans = mutableListOf<Scan>()
        if (buildSystems.contains("bazel")) {
            for (i in 1..10) {
                scans.add(Scan(id = i.toString(), buildToolType = "bazel"))
            }
            for (i in 1..filter.maxBuilds - 10) {
                scans.add(Scan(id = i.toString(), buildToolType = "gradle"))
            }
        } else if (buildSystems.contains("maven")) {
            for (i in 1..filter.maxBuilds / 2) {
                scans.add(Scan(id = i.toString(), buildToolType = "maven"))
            }
            for (i in 1..filter.maxBuilds / 2) {
                scans.add(Scan(id = i.toString(), buildToolType = "gradle"))
            }
        } else {
            for (i in 1..filter.maxBuilds) {
                scans.add(Scan(id = i.toString(), buildToolType = "gradle"))
            }
        }

        return scans.toTypedArray()
    }

    override suspend fun getBuildScanGradleAttribute(id: String): GradleScan {
        return GradleScan(
            id = id,
            buildStartTime = System.currentTimeMillis(),
            buildDuration = 10L,
            hasFailed = false,
            environment = Environment("kio", "3"),
            values = emptyArray(),
            requestedTasks = emptyArray(),
            rootProjectName = "nowinandroid",
            tags = arrayOf("tag1"),
        )
    }

    override suspend fun getBuildScanMavenAttribute(id: String): MavenScan {
        return MavenScan(
            id = id,
            buildStartTime = System.currentTimeMillis(),
            buildDuration = 10L,
            hasFailed = false,
            environment = Environment("kio", "3"),
            values = emptyArray(),
            requestedGoals = emptyArray(),
            topLevelProjectName = "nowinandroid",
            tags = arrayOf("tag1"),
        )
    }

    override suspend fun getBuildScanGradleCachePerformance(id: String): Build {
        TODO("Not yet implemented")
    }

    override suspend fun getBuildScanMavenCachePerformance(id: String): Build {
        TODO("Not yet implemented")
    }

    override suspend fun getArtifactTransformRequest(id: String): ArtifactTransforms {
        TODO("Not yet implemented")
    }

    override suspend fun getBuildScanGradlePerformance(id: String): BuildWithResourceUsage {
        TODO("Not yet implemented")
    }

    override suspend fun getConfigurationCacheResult(id: String): ConfigurationCacheResult {
        TODO("Not yet implemented")
    }
}

internal class FakeEmptyRepositoryWithQuery : FakeTestRepository() {
    override suspend fun getBuildScansWithAdvancedQuery(
        filter: Filter,
        buildId: String?,
    ): Array<Scan> {
        return emptyArray<Scan>()
    }

    override suspend fun getBuildScanGradleAttribute(id: String): GradleScan {
        return GradleScan(
            id = id,
            buildStartTime = System.currentTimeMillis(),
            buildDuration = 10L,
            hasFailed = false,
            environment = Environment("kio", "3"),
            values = emptyArray(),
            requestedTasks = emptyArray(),
            rootProjectName = "nowinandroid",
            tags = arrayOf("tag1"),
        )
    }

    override suspend fun getBuildScanMavenAttribute(id: String): MavenScan {
        return MavenScan(
            id = id,
            buildStartTime = System.currentTimeMillis(),
            buildDuration = 10L,
            hasFailed = false,
            environment = Environment("kio", "3"),
            values = emptyArray(),
            requestedGoals = emptyArray(),
            topLevelProjectName = "nowinandroid",
            tags = arrayOf("tag1"),
        )
    }
}
