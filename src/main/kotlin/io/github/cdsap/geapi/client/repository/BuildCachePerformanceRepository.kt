package io.github.cdsap.geapi.client.repository

import io.github.cdsap.geapi.client.model.Build

interface BuildCachePerformanceRepository {
    suspend fun getBuildScanGradleCachePerformance(id: String): Build

    suspend fun getBuildScanMavenCachePerformance(id: String): Build
}
