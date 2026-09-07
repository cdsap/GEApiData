package io.github.cdsap.geapi.client.domain.impl.mapper

import io.github.cdsap.geapi.client.model.Build
import io.github.cdsap.geapi.client.model.BuildWithResourceUsage
import io.github.cdsap.geapi.client.model.GradleScan
import io.github.cdsap.geapi.client.model.MavenScan
import io.github.cdsap.geapi.client.model.ScanWithAttributes

class ScanMapper {
    fun scanWithAttributes(
        gradleScan: GradleScan?,
        mavenScan: MavenScan?,
    ): ScanWithAttributes {
        return if (gradleScan != null) {
            ScanWithAttributes(
                buildTool = "gradle",
                id = gradleScan.id,
                projectName = gradleScan.rootProjectName ?: "",
                requestedTasksGoals = gradleScan.requestedTasks,
                tags = gradleScan.tags,
                hasFailed = gradleScan.hasFailed,
                environment = gradleScan.environment,
                buildDuration = gradleScan.buildDuration,
                buildStartTime = gradleScan.buildStartTime,
                values = gradleScan.values,
            )
        } else {
            ScanWithAttributes(
                buildTool = "maven",
                id = mavenScan!!.id,
                projectName = mavenScan.topLevelProjectName ?: "",
                requestedTasksGoals = mavenScan.requestedGoals,
                tags = mavenScan.tags,
                hasFailed = mavenScan.hasFailed,
                environment = mavenScan.environment,
                buildDuration = mavenScan.buildDuration,
                buildStartTime = mavenScan.buildStartTime,
                values = mavenScan.values,
            )
        }
    }

    fun enrichBuildWithScanMetadata(
        build: Build,
        scan: ScanWithAttributes,
    ) {
        build.builtTool = scan.buildTool
        build.buildStartTime = scan.buildStartTime
        build.tags = scan.tags
        build.projectName = scan.projectName
        build.requestedTask = scan.requestedTasksGoals
        build.buildDuration = scan.buildDuration
        build.values = scan.values
    }

    fun enrichBuildWithResourceUsageWithScanMetadata(
        buildWithResourceUsage: BuildWithResourceUsage,
        scan: ScanWithAttributes,
    ) {
        buildWithResourceUsage.id = scan.id
        buildWithResourceUsage.buildDuration = scan.buildDuration
        buildWithResourceUsage.buildStartTime = scan.buildStartTime
        buildWithResourceUsage.requestedTask = scan.requestedTasksGoals
        buildWithResourceUsage.builtTool = scan.buildTool
        buildWithResourceUsage.projectName = scan.projectName
        buildWithResourceUsage.tags = scan.tags
        buildWithResourceUsage.environment = scan.environment
        buildWithResourceUsage.values = scan.values
    }
}
