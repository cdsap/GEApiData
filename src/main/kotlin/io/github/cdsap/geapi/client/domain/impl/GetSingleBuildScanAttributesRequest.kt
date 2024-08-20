package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.domain.GetSingleBuildScanAttributes
import io.github.cdsap.geapi.client.domain.impl.mapper.ScanMapper
import io.github.cdsap.geapi.client.model.ScanWithAttributes
import io.github.cdsap.geapi.client.repository.GradleEnterpriseRepository

class GetSingleBuildScanAttributesRequest(private val repository: GradleEnterpriseRepository) :
    GetSingleBuildScanAttributes {
    override suspend fun get(buildId: String): ScanWithAttributes {
        return ScanMapper().scanWithAttributes(repository.getBuildScanGradleAttribute(buildId), null)
    }
}
