package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.domain.GetBuildsWithCachePerformance
import io.github.cdsap.geapi.client.domain.impl.logger.Logger
import io.github.cdsap.geapi.client.domain.impl.progress.ProgressFeedback
import io.github.cdsap.geapi.client.model.Build
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.model.ScanWithAttributes
import io.github.cdsap.geapi.client.repository.GradleEnterpriseRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class GetBuildsWithCachePerformanceRequest(private val repository: GradleEnterpriseRepository) : GetBuildsWithCachePerformance {
    override suspend fun get(
        builds: List<ScanWithAttributes>,
        filter: Filter,
    ): List<Build> {
        return if (builds.isNotEmpty()) {
            cachePerformanceBuilds(builds, filter, Logger(filter.clientType))
        } else {
            emptyList()
        }
    }

    private suspend fun cachePerformanceBuilds(
        builds: List<ScanWithAttributes>,
        filter: Filter,
        logger: Logger,
    ): List<Build> {
        logger.log("Processing build scan cache performance for ${builds.size} builds")

        val cachePerformanceBuild = mutableListOf<Build>()
        val duration = System.currentTimeMillis().toDuration(DurationUnit.MILLISECONDS)
        val progressFeedback = ProgressFeedback(filter.clientType, builds.size)
        val semaphore = Semaphore(filter.concurrentCallsConservative)

        progressFeedback.init()

        coroutineScope {
            val runningTasks =
                builds.map {
                    async {
                        semaphore.acquire()
                        val cachePerformance =
                            if (it.buildTool == "gradle") {
                                repository.getBuildScanGradleCachePerformance(it.id)
                            } else {
                                repository.getBuildScanMavenCachePerformance(it.id)
                            }.apply {
                                map(this, it)
                            }
                        progressFeedback.update()
                        semaphore.release()
                        cachePerformance
                    }
                }
            cachePerformanceBuild.addAll(runningTasks.awaitAll())
        }
        logger.log(
            "\nGetting cache performance builds in: " + (
                System.currentTimeMillis()
                    .toDuration(DurationUnit.MILLISECONDS) - duration
            ),
        )
        return cachePerformanceBuild
    }

    private fun map(
        cachePerformance: Build,
        scan: ScanWithAttributes,
    ) {
        cachePerformance.builtTool = scan.buildTool
        cachePerformance.buildStartTime = scan.buildStartTime
        cachePerformance.tags = scan.tags
        cachePerformance.projectName = scan.projectName
        cachePerformance.requestedTask = scan.requestedTasksGoals
        cachePerformance.buildDuration = scan.buildDuration
        cachePerformance.values = scan.values
    }
}
