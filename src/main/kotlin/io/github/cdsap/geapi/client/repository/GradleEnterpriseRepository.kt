package io.github.cdsap.geapi.client.repository

import io.github.cdsap.geapi.client.model.*
import io.github.cdsap.geapi.domain.model.*

interface GradleEnterpriseRepository {

    suspend fun getBuildScans(filter: Filter, buildId: String? = null): Array<Scan>

    suspend fun getBuildScanGradleAttribute(id: String): ScanWithAttributesGradle
    suspend fun getBuildScanMavenAttribute(id: String): ScanWithAttributesMaven

    suspend fun getBuildScanGradleCachePerformance(id: String): Build
    suspend fun getBuildScanMavenCachePerformance(id: String): Build
}
