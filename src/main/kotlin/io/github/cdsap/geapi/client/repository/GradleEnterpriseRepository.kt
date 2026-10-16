package io.github.cdsap.geapi.client.repository

import io.github.cdsap.geapi.client.model.ArtifactTransforms
import io.github.cdsap.geapi.client.model.Build
import io.github.cdsap.geapi.client.model.BuildWithResourceUsage
import io.github.cdsap.geapi.client.model.ConfigurationCacheResult
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.model.Scan

interface GradleEnterpriseRepository : BuildProfileRepository, BuildScanAttributesRepository {
    suspend fun getBuildScans(
        filter: Filter,
        buildId: String? = null,
    ): Array<Scan>

    suspend fun getBuildScansWithAdvancedQuery(
        filter: Filter,
        query: String,
        buildId: String? = null,
    ): Array<Scan>

    suspend fun getBuildScanGradleCachePerformance(id: String): Build

    suspend fun getBuildScanMavenCachePerformance(id: String): Build

    suspend fun getArtifactTransformRequest(id: String): ArtifactTransforms

    suspend fun getBuildScanGradlePerformance(id: String): BuildWithResourceUsage

    suspend fun getConfigurationCacheResult(id: String): ConfigurationCacheResult

}
