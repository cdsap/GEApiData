package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.model.ClientType
import io.github.cdsap.geapi.client.model.Environment
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.model.ScanWithAttributes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FilterBuildScanTest {

    private lateinit var filterBuildScan: FilterBuildScan

    @BeforeEach
    fun setUp() {
        filterBuildScan = FilterBuildScan()
    }

    @Test
    fun testFilter_withMatchingAttributes_shouldReturnTrue() {
        val scanAttributes = ScanWithAttributes(
            id = "1",
            projectName = "MyProject",
            requestedTasksGoals = arrayOf("clean", "build"),
            tags = arrayOf("tag1", "tag2"),
            hasFailed = false,
            environment = Environment(username = "user1"),
            buildDuration = 1000,
            buildTool = "gradle",
            buildStartTime = System.currentTimeMillis(),
            values = emptyArray()
        )

        val filter = Filter(
            includeFailedBuilds = false,
            project = "MyProject",
            requestedTask = "build",
            tags = listOf("tag1"),
            user = "user1",
            clientType = ClientType.API,
            concurrentCallsConservative = 0
        )

        val result = filterBuildScan.filter(scanAttributes, filter)

        assertEquals(true, result)
    }

    @Test
    fun testFilter_withNonMatchingProject_shouldReturnFalse() {
        val scanAttributes = ScanWithAttributes(
            id = "2",
            projectName = "AnotherProject",
            requestedTasksGoals = arrayOf("test"),
            tags = arrayOf("tag3"),
            hasFailed = true,
            environment = Environment(username = "user2"),
            buildDuration = 1500,
            buildTool = "maven",
            buildStartTime = System.currentTimeMillis(),
            values = emptyArray()
        )

        val filter = Filter(
            includeFailedBuilds = false,
            project = "MyProject",
            requestedTask = "build",
            tags = listOf("tag1"),
            user = "user1",
            clientType = ClientType.API,
            concurrentCallsConservative = 0
        )

        val result = filterBuildScan.filter(scanAttributes, filter)

        assertEquals(false, result)
    }

    @Test
    fun testFilter_withNotMatchingUser_shouldReturnFalse() {
        val scanAttributes = ScanWithAttributes(
            id = "1",
            projectName = "MyProject",
            requestedTasksGoals = arrayOf("clean", "build"),
            tags = arrayOf("tag1", "tag2"),
            hasFailed = false,
            environment = Environment(username = "user1"),
            buildDuration = 1000,
            buildTool = "gradle",
            buildStartTime = System.currentTimeMillis(),
            values = emptyArray()
        )

        val filter = Filter(
            includeFailedBuilds = false,
            project = "MyProject",
            requestedTask = "build",
            tags = listOf("tag1"),
            user = "user2",
            clientType = ClientType.API,
            concurrentCallsConservative = 0
        )

        val result = filterBuildScan.filter(scanAttributes, filter)

        assertEquals(false, result)
    }
}
