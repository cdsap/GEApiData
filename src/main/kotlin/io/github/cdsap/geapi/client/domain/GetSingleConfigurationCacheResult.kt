package io.github.cdsap.geapi.client.domain

import io.github.cdsap.geapi.client.model.ConfigurationCacheResult

interface GetSingleConfigurationCacheResult {
    suspend fun get(buildId: String): ConfigurationCacheResult
}
