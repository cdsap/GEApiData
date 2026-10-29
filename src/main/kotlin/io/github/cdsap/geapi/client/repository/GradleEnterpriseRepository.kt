package io.github.cdsap.geapi.client.repository

import io.github.cdsap.geapi.client.model.ArtifactTransforms
import io.github.cdsap.geapi.client.model.Build
import io.github.cdsap.geapi.client.model.BuildWithResourceUsage
import io.github.cdsap.geapi.client.model.ConfigurationCacheResult

interface GradleEnterpriseRepository : BuildProfileRepository, BuildScanRepository {

    suspend fun getBuildScanGradleCachePerformance(id: String): Build

    suspend fun getBuildScanMavenCachePerformance(id: String): Build

    suspend fun getArtifactTransformRequest(id: String): ArtifactTransforms

    suspend fun getBuildScanGradlePerformance(id: String): BuildWithResourceUsage

    suspend fun getConfigurationCacheResult(id: String): ConfigurationCacheResult

}
