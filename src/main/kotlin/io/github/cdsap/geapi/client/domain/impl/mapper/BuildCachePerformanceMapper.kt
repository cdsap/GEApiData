package io.github.cdsap.geapi.client.domain.impl.mapper

import io.github.cdsap.geapi.client.model.Build
import io.github.cdsap.geapi.client.model.ScanWithAttributes

class BuildCachePerformanceMapper {
    fun enrichBuildWithScanMetadata(
        cachePerformance: Build,
        scan: ScanWithAttributes,
    ) {
        cachePerformance.builtTool = scan.buildTool
        cachePerformance.buildStartTime = scan.buildStartTime
        cachePerformance.tags = scan.tags
        cachePerformance.projectName = scan.projectName
        cachePerformance.requestedTask = scan.requestedTasksGoals
        cachePerformance.buildDuration = scan.buildDuration
        cachePerformance.values = scan.values
    }
}
