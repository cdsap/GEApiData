package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.model.GradleScan
import io.github.cdsap.geapi.client.model.MavenScan
import io.github.cdsap.geapi.client.model.ScanWithAttributes

class ScanMapper {
    fun scanWithAttributes(
        gradleScan: GradleScan?,
        mavenScan: MavenScan?
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
                values = gradleScan.values
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
                values = mavenScan.values
            )
        }
    }
}
