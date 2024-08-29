package io.github.cdsap.geapi.client.domain

import io.github.cdsap.geapi.client.model.PerformanceUsage

interface GetSingleGradleResourceUsage {
    suspend fun get(
        buildId: String
    ): PerformanceUsage
}
