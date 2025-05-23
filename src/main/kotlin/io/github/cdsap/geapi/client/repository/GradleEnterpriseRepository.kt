package io.github.cdsap.geapi.client.repository

import io.github.cdsap.geapi.client.model.ArtifactTransforms
import io.github.cdsap.geapi.client.model.Build
import io.github.cdsap.geapi.client.model.BuildProfileOverview
import io.github.cdsap.geapi.client.model.BuildWithResourceUsage
import io.github.cdsap.geapi.client.model.ConfigurationCacheResult
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.model.GradleScan
import io.github.cdsap.geapi.client.model.MavenScan
import io.github.cdsap.geapi.client.model.Scan

interface GradleEnterpriseRepository {
    suspend fun getBuildScans(
        filter: Filter,
        buildId: String? = null,
    ): Array<Scan>

    suspend fun getBuildScansWithAdvancedQuery(
        filter: Filter,
        buildId: String? = null,
    ): Array<Scan>

    suspend fun getBuildScanGradleAttribute(id: String): GradleScan

    suspend fun getBuildScanMavenAttribute(id: String): MavenScan

    suspend fun getBuildScanGradleCachePerformance(id: String): Build

    suspend fun getBuildScanMavenCachePerformance(id: String): Build

    suspend fun getArtifactTransformRequest(id: String): ArtifactTransforms

    suspend fun getBuildScanGradlePerformance(id: String): BuildWithResourceUsage

    suspend fun getConfigurationCacheResult(id: String): ConfigurationCacheResult

    suspend fun getBuildProfileOverview(id: String): BuildProfileOverview
}
