package io.github.cdsap.geapi.client.repository

import io.github.cdsap.geapi.client.model.ArtifactTransforms
import io.github.cdsap.geapi.client.model.BuildWithResourceUsage
import io.github.cdsap.geapi.client.model.ConfigurationCacheResult

interface GradleEnterpriseRepository :
    BuildProfileRepository,
    BuildScanRepository,
    BuildCachePerformanceRepository {

    suspend fun getArtifactTransformRequest(id: String): ArtifactTransforms

    suspend fun getBuildScanGradlePerformance(id: String): BuildWithResourceUsage

    suspend fun getConfigurationCacheResult(id: String): ConfigurationCacheResult

}
