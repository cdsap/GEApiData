package io.github.cdsap.geapi.client.repository

import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.model.Scan

interface BuildScanRepository : BuildScanAttributesRepository {
    suspend fun getBuildScans(
        filter: Filter,
        buildId: String? = null,
    ): Array<Scan>

    suspend fun getBuildScansWithAdvancedQuery(
        filter: Filter,
        query: String,
        buildId: String? = null,
    ): Array<Scan>
}
