package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.domain.GetSingleBuildCachePerformance
import io.github.cdsap.geapi.client.model.Build
import io.github.cdsap.geapi.client.repository.GradleEnterpriseRepository

class GetSingleBuildCachePerformanceRequest(private val repository: GradleEnterpriseRepository) :
    GetSingleBuildCachePerformance {
    override suspend fun get(buildId: String): Build {
        return repository.getBuildScanGradleCachePerformance(buildId)
    }
}
