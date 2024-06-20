package io.github.cdsap.geapi.client.domain

import io.github.cdsap.geapi.client.model.ScanWithAttributes

interface GetSingleBuildScanAttributes {
    suspend fun get(buildId: String): ScanWithAttributes
}
