package io.github.cdsap.geapi.client.domain

import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.model.ScanWithAttributes

interface GetBuildScansWithFilter {
    suspend fun get(query: Filter): List<ScanWithAttributes>
}
