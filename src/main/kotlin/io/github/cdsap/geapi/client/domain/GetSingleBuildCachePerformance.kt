package io.github.cdsap.geapi.client.domain

import io.github.cdsap.geapi.client.model.Build
import io.github.cdsap.geapi.client.model.ScanWithAttributes

interface GetSingleBuildCachePerformance {
    suspend fun get(buildId: String): Build
}
