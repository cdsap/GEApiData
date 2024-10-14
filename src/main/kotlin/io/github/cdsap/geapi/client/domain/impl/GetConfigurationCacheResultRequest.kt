package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.domain.GetConfigurationCacheResult
import io.github.cdsap.geapi.client.domain.impl.logger.Logger
import io.github.cdsap.geapi.client.domain.impl.progress.ProgressFeedback
import io.github.cdsap.geapi.client.model.ConfigurationCacheResult
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.model.ScanWithAttributes
import io.github.cdsap.geapi.client.repository.GradleEnterpriseRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
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

        val configurationCacheResults = mutableListOf<ConfigurationCacheResult>()
        val duration = System.currentTimeMillis().toDuration(DurationUnit.MILLISECONDS)
        val progressFeedback = ProgressFeedback(filter.clientType, builds.size)
        val semaphore = Semaphore(filter.concurrentCallsConservative)

        progressFeedback.init()

        coroutineScope {
            val runningTasks =
                builds.filter { it.buildTool == "gradle" }.map {
                    async {
                        semaphore.acquire()
                        val scanAttributes = it
                        val configurationCacheResult = repository.getConfigurationCacheResult(scanAttributes.id)
                        progressFeedback.update()
                        semaphore.release()
                        configurationCacheResult
                    }
                }
            configurationCacheResults.addAll(runningTasks.awaitAll())
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
