package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.domain.GetSingleGradleResourceUsage
import io.github.cdsap.geapi.client.model.BuildWithResourceUsage
import io.github.cdsap.geapi.client.repository.GradleEnterpriseRepository

class GetSingleBuildResourceUsageRequest(private val repository: GradleEnterpriseRepository) :
    GetSingleGradleResourceUsage {
    override suspend fun get(buildId: String): BuildWithResourceUsage {
        return repository.getBuildScanGradlePerformance(buildId)
    }
}
