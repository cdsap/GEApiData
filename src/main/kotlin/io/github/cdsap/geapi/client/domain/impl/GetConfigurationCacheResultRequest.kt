package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.domain.GetConfigurationCacheResult
import io.github.cdsap.geapi.client.domain.impl.logger.Logger
import io.github.cdsap.geapi.client.model.ConfigurationCacheResult
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.model.ScanWithAttributes
import io.github.cdsap.geapi.client.repository.GradleEnterpriseRepository
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class GetConfigurationCacheResultRequest(private val repository: GradleEnterpriseRepository) : GetConfigurationCacheResult {
    override suspend fun get(
        builds: List<ScanWithAttributes>,
        filter: Filter,
    ): List<ConfigurationCacheResult> {
        return if (builds.isNotEmpty()) {
            processConfigurationCacheResult(builds, filter, Logger(filter.clientType))
        } else {
            emptyList()
        }
    }

    private suspend fun processConfigurationCacheResult(
        builds: List<ScanWithAttributes>,
        filter: Filter,
        logger: Logger,
    ): List<ConfigurationCacheResult> {
        logger.log("Processing configuration cache results for ${builds.size} builds")

        val duration = System.currentTimeMillis().toDuration(DurationUnit.MILLISECONDS)
        val configurationCacheResults =
            BuildScanBatchProcessor.process(
                builds = builds,
                filter = filter,
                predicate = { it.buildTool == "gradle" },
            ) { scanAttributes ->
                repository.getConfigurationCacheResult(scanAttributes.id)
            }
        logger.log(
            "Getting configuration cache result in: " + (
                System.currentTimeMillis()
                    .toDuration(DurationUnit.MILLISECONDS) - duration
            ),
        )
        return configurationCacheResults
    }
}
