package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.domain.GetGradleResourceUsage
import io.github.cdsap.geapi.client.domain.impl.logger.Logger
import io.github.cdsap.geapi.client.domain.impl.progress.ProgressFeedback
import io.github.cdsap.geapi.client.model.BuildWithResourceUsage
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.model.ScanWithAttributes
import io.github.cdsap.geapi.client.repository.GradleEnterpriseRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class GetBuildsResourceUsageRequest(private val repository: GradleEnterpriseRepository) : GetGradleResourceUsage {
    override suspend fun get(
        builds: List<ScanWithAttributes>,
        filter: Filter,
    ): List<BuildWithResourceUsage> {
        return if (builds.isNotEmpty()) {
            processUsage(builds, filter, Logger(filter.clientType))
        } else {
            emptyList()
        }
    }

    private suspend fun processUsage(
        builds: List<ScanWithAttributes>,
        filter: Filter,
        logger: Logger,
    ): List<BuildWithResourceUsage> {
        logger.log("Processing build scan resource usages for ${builds.size} builds")

        val resourceUsages = mutableListOf<BuildWithResourceUsage>()
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

                        val processUsage =
                            repository.getBuildScanGradlePerformance(scanAttributes.id)
                        processUsage.id = scanAttributes.id
                        processUsage.buildDuration = scanAttributes.buildDuration
                        processUsage.buildStartTime = scanAttributes.buildStartTime
                        processUsage.requestedTask = scanAttributes.requestedTasksGoals
                        processUsage.builtTool = scanAttributes.buildTool
                        processUsage.projectName = scanAttributes.projectName
                        processUsage.tags = scanAttributes.tags
                        processUsage.environment = scanAttributes.environment
                        processUsage.values = scanAttributes.values

                        progressFeedback.update()
                        semaphore.release()
                        processUsage
                    }
                }
            resourceUsages.addAll(runningTasks.awaitAll())
        }
        logger.log(
            "Getting resources usage builds in: " + (
                System.currentTimeMillis()
                    .toDuration(DurationUnit.MILLISECONDS) - duration
            ),
        )
        return resourceUsages
    }
}
