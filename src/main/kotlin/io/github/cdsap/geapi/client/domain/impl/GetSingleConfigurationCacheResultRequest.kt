package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.domain.GetSingleConfigurationCacheResult
import io.github.cdsap.geapi.client.model.ConfigurationCacheResult
import io.github.cdsap.geapi.client.repository.GradleEnterpriseRepository

class GetSingleConfigurationCacheResultRequest(private val repository: GradleEnterpriseRepository) :
    GetSingleConfigurationCacheResult {
    override suspend fun get(buildId: String): ConfigurationCacheResult {
        return repository.getConfigurationCacheResult(buildId)
    }
}
