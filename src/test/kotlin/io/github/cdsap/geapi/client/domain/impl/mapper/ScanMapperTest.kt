package io.github.cdsap.geapi.client.domain.impl.mapper

import io.github.cdsap.geapi.client.model.AvoidanceSavingsSummary
import io.github.cdsap.geapi.client.model.Build
import io.github.cdsap.geapi.client.model.BuildWithResourceUsage
import io.github.cdsap.geapi.client.model.CustomValue
import io.github.cdsap.geapi.client.model.Environment
import io.github.cdsap.geapi.client.model.GradleScan
import io.github.cdsap.geapi.client.model.MavenScan
import io.github.cdsap.geapi.client.model.Metric
import io.github.cdsap.geapi.client.model.PerformanceMetrics
import io.github.cdsap.geapi.client.model.ScanWithAttributes
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ScanMapperTest {
    private val environment = Environment(username = "user", numberOfCpuCores = "4")
    private val gradleScan =
        GradleScan(
            id = "gradle-id", rootProjectName = "gradle-project", requestedTasks = arrayOf("test"),
            tags = arrayOf("gradle-tag"), hasFailed = true, environment = environment,
            buildDuration = 123, buildStartTime = 456, values = arrayOf(CustomValue("g", "1")),
        )
    private val mavenScan =
        MavenScan(
            id = "maven-id", topLevelProjectName = "maven-project", requestedGoals = arrayOf("verify"),
            tags = arrayOf("maven-tag"), hasFailed = false, environment = environment,
            buildDuration = 789, buildStartTime = 101, values = arrayOf(CustomValue("m", "2")),
        )

    @Test
    fun mapsTypedGradleScan() {
        val result = ScanMapper().scanWithAttributes(gradleScan)

        assertEquals("gradle", result.buildTool)
        assertEquals("gradle-id", result.id)
        assertEquals("gradle-project", result.projectName)
        assertArrayEquals(arrayOf("test"), result.requestedTasksGoals)
        assertArrayEquals(arrayOf("gradle-tag"), result.tags)
        assertEquals(true, result.hasFailed)
        assertEquals(environment, result.environment)
        assertEquals(123L, result.buildDuration)
        assertEquals(456L, result.buildStartTime)
        assertArrayEquals(gradleScan.values, result.values)
    }

    @Test
    fun mapsTypedMavenScan() {
        val result = ScanMapper().scanWithAttributes(mavenScan)

        assertEquals("maven", result.buildTool)
        assertEquals("maven-id", result.id)
        assertEquals("maven-project", result.projectName)
        assertArrayEquals(arrayOf("verify"), result.requestedTasksGoals)
        assertArrayEquals(arrayOf("maven-tag"), result.tags)
        assertEquals(false, result.hasFailed)
        assertEquals(environment, result.environment)
        assertEquals(789L, result.buildDuration)
        assertEquals(101L, result.buildStartTime)
        assertArrayEquals(mavenScan.values, result.values)
    }

    @Test
    fun nullablePairRetainsCompatibility() {
        val mapper = ScanMapper()

        assertEquals(mapper.scanWithAttributes(gradleScan), mapper.scanWithAttributes(gradleScan, null))
        assertEquals(mapper.scanWithAttributes(mavenScan), mapper.scanWithAttributes(null, mavenScan))
        assertEquals(mapper.scanWithAttributes(gradleScan), mapper.scanWithAttributes(gradleScan, mavenScan))
        assertThrows(NullPointerException::class.java) { mapper.scanWithAttributes(null, null) }
    }

    @Test
    fun enrichBuildWithScanMetadataCopiesScanAttributesOntoBuild() {
        val build =
            Build(
                builtTool = "gradle",
                taskExecution = emptyArray(),
                id = "build-id",
                buildDuration = 24,
                avoidanceSavingsSummary = AvoidanceSavingsSummary("12", "1", "1"),
                goalExecution = emptyArray(),
            )
        val scan =
            ScanWithAttributes(
                id = "scan-id",
                projectName = "AnotherProject",
                requestedTasksGoals = arrayOf("test"),
                tags = arrayOf("tag3"),
                hasFailed = true,
                environment = Environment(username = "user2", numberOfCpuCores = "3"),
                buildDuration = 1500,
                buildTool = "maven",
                buildStartTime = 1789,
                values = arrayOf(CustomValue("a", "b")),
            )

        ScanMapper().enrichBuildWithScanMetadata(build, scan)

        assertEquals("maven", build.builtTool)
        assertEquals(1789, build.buildStartTime)
        assertArrayEquals(arrayOf("tag3"), build.tags)
        assertEquals("AnotherProject", build.projectName)
        assertArrayEquals(arrayOf("test"), build.requestedTask)
        assertEquals(1500, build.buildDuration)
        assertEquals("a", build.values[0].name)
        assertEquals("b", build.values[0].value)
        assertEquals("build-id", build.id)
    }

    @Test
    fun enrichBuildWithResourceUsageWithScanMetadataCopiesScanAttributesOntoBuildWithResourceUsage() {
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
        val buildWithResourceUsage =
            BuildWithResourceUsage(
                totalMemory = 1000L,
                total = performanceMetrics,
                execution = performanceMetrics,
                nonExecution = performanceMetrics,
            )
        val scan =
            ScanWithAttributes(
                id = "scan-id",
                projectName = "AnotherProject",
                requestedTasksGoals = arrayOf("test"),
                tags = arrayOf("tag3"),
                hasFailed = true,
                environment = Environment(username = "user2", numberOfCpuCores = "3"),
                buildDuration = 1500,
                buildTool = "gradle",
                buildStartTime = 1789,
                values = arrayOf(CustomValue("a", "b")),
            )

        ScanMapper().enrichBuildWithResourceUsageWithScanMetadata(buildWithResourceUsage, scan)

        assertEquals("scan-id", buildWithResourceUsage.id)
        assertEquals("gradle", buildWithResourceUsage.builtTool)
        assertEquals(1789, buildWithResourceUsage.buildStartTime)
        assertArrayEquals(arrayOf("tag3"), buildWithResourceUsage.tags)
        assertEquals("AnotherProject", buildWithResourceUsage.projectName)
        assertArrayEquals(arrayOf("test"), buildWithResourceUsage.requestedTask)
        assertEquals(1500, buildWithResourceUsage.buildDuration)
        assertEquals("user2", buildWithResourceUsage.environment.username)
        assertEquals("a", buildWithResourceUsage.values[0].name)
        assertEquals("b", buildWithResourceUsage.values[0].value)
    }
}
