package io.github.cdsap.geapi.client.domain.impl.mapper

import io.github.cdsap.geapi.client.model.AvoidanceSavingsSummary
import io.github.cdsap.geapi.client.model.Build
import io.github.cdsap.geapi.client.model.CustomValue
import io.github.cdsap.geapi.client.model.Environment
import io.github.cdsap.geapi.client.model.ScanWithAttributes
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ScanMapperTest {
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
}
