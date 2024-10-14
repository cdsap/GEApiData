package io.github.cdsap.geapi.client.domain

import io.github.cdsap.geapi.client.model.ConfigurationCacheResult
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.model.ScanWithAttributes

interface GetConfigurationCacheResult {
    suspend fun get(
        builds: List<ScanWithAttributes>,
        filter: Filter = Filter(),
    ): List<ConfigurationCacheResult>
}
