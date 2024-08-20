package io.github.cdsap.geapi.client.domain

import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.model.PerformanceUsage
import io.github.cdsap.geapi.client.model.ScanWithAttributes

interface GetGradleResourceUsage {
    suspend fun get(
        builds: List<ScanWithAttributes>,
        filter: Filter,
    ): List<PerformanceUsage>
}
