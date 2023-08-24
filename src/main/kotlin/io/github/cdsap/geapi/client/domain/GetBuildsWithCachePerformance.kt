package io.github.cdsap.geapi.client.domain

import io.github.cdsap.geapi.client.model.Build
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.model.ScanWithAttributes

interface GetBuildsWithCachePerformance {
    suspend fun get(builds: List<ScanWithAttributes>, filter: Filter): List<Build>
}
