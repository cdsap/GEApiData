package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.domain.impl.logger.Logger
import io.github.cdsap.geapi.client.model.CustomValue
import io.github.cdsap.geapi.client.model.Environment
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.model.GradleScan
import io.github.cdsap.geapi.client.model.MavenScan
import io.github.cdsap.geapi.client.model.Scan
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

class GetScanAttributeTest {
    @Test
    fun usesWithPermitInsteadOfManualAcquireRelease() {
        val source =
            File("src/main/kotlin/io/github/cdsap/geapi/client/domain/impl/GetScanAttribute.kt")
                .readText()

        assertTrue(source.contains("withPermit"))
        assertFalse(source.contains("semaphore.acquire()"))
        assertFalse(source.contains("semaphore.release()"))
    }

    @Test
    fun returnsEmptyListWhenNoBuildsAreProvided() =
        runBlocking {
            val request = GetScanAttribute(SucceedingScanAttributeRepository())
            val filter = Filter()

            val result = request.getScanAttributes(emptyList(), filter, Logger(filter.clientType))

            assertEquals(emptyList<ScanWithAttributes>(), result)
        }

    @Test
    fun returnsAttributesForGradleAndMavenScansAndFiltersOtherBuildTools() =
        runBlocking {
            val request = GetScanAttribute(SucceedingScanAttributeRepository())
            val filter = Filter(concurrentCalls = 1)
            val scans =
                listOf(
                    Scan(id = "gradle-1", buildToolType = "gradle"),
                    Scan(id = "bazel-1", buildToolType = "bazel"),
                    Scan(id = "maven-1", buildToolType = "maven"),
                    Scan(id = "gradle-2", buildToolType = "gradle"),
                )

            val result = request.getScanAttributes(scans, filter, Logger(filter.clientType))

            assertEquals(3, result.size)
            assertEquals(listOf("gradle-1", "maven-1", "gradle-2"), result.map { it.id })
            assertEquals(listOf("gradle", "maven", "gradle"), result.map { it.buildTool })
            assertEquals("gradle-project", result[0].projectName)
            assertEquals("maven-project", result[1].projectName)
        }

    @Test
    fun propagatesRepositoryFailureWithoutDependingOnHappyPathPermitRelease() =
        runBlocking {
            val request = GetScanAttribute(FailingScanAttributeOverviewRepository())
            val filter = Filter(concurrentCalls = 1)

            val thrown =
                assertFailsWith<RuntimeException> {
                    withTimeout(5.seconds) {
                        request.getScanAttributes(
                            listOf(
                                Scan(id = "scan-1", buildToolType = "gradle"),
                                Scan(id = "scan-2", buildToolType = "gradle"),
                                Scan(id = "scan-3", buildToolType = "gradle"),
                            ),
                            filter,
                            Logger(filter.clientType),
                        )
                    }
                }

            assertEquals("repository failure", thrown.message)
        }
}

private class SucceedingScanAttributeRepository : FakeTestRepository() {
    override suspend fun getBuildScanGradleAttribute(id: String): GradleScan {
        return GradleScan(
            id = id,
            rootProjectName = "gradle-project",
            requestedTasks = arrayOf("test"),
            tags = arrayOf("tag"),
            hasFailed = false,
            environment = Environment(username = "user", numberOfCpuCores = "2"),
            buildDuration = 100,
            buildStartTime = 1,
            values = arrayOf(CustomValue("k", "v")),
        )
    }

    override suspend fun getBuildScanMavenAttribute(id: String): MavenScan {
        return MavenScan(
            id = id,
            topLevelProjectName = "maven-project",
            requestedGoals = arrayOf("verify"),
            tags = arrayOf("tag"),
            hasFailed = false,
            environment = Environment(username = "user", numberOfCpuCores = "2"),
            buildDuration = 100,
            buildStartTime = 1,
            values = arrayOf(CustomValue("k", "v")),
        )
    }
}

private class FailingScanAttributeOverviewRepository : FakeTestRepository() {
    override suspend fun getBuildScanGradleAttribute(id: String): GradleScan {
        throw RuntimeException("repository failure")
    }
}
