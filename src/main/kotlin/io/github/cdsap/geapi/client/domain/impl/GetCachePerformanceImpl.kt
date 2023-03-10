package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.domain.GetCachePerformance
import io.github.cdsap.geapi.client.model.Build
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.model.ScanWithAttributes
import io.github.cdsap.geapi.client.repository.GradleEnterpriseRepository
import io.github.cdsap.geapi.progressbar.ProgressBar
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class GetCachePerformanceImpl(private val repository: GradleEnterpriseRepository) : GetCachePerformance {

    override suspend fun get(builds: List<ScanWithAttributes>, filter: Filter): List<Build> {
        val cachePerformanceBuild = mutableListOf<Build>()
        if (builds.isNotEmpty()) {
            println("Processing build scan cache performance for ${builds.size} builds")
            val progressBar = ProgressBar()
            val duration = System.currentTimeMillis().toDuration(DurationUnit.MILLISECONDS)
            progressBar.update(0, builds.size)
            var i = 0
            val semaphore = Semaphore(filter.concurrentCallsConservative)
            coroutineScope {
                val runningTasks = builds.map {
                    async {
                        semaphore.acquire()
                        val cachePerformance = if (it.buildTool == "gradle") {
                            repository.getBuildScanGradleCachePerformance(it.id)
                        } else {
                            repository.getBuildScanMavenCachePerformance(it.id)

                        }
                        cachePerformance.builtTool = it.buildTool
                        cachePerformance.buildStartTime = it.buildStartTime
                        cachePerformance.tags = it.tags
                        cachePerformance.projectName = it.projectName
                        cachePerformance.requestedTask = it.requestedTasksGoals
                        progressBar.update(i++, builds.size)
                        semaphore.release()
                        cachePerformance
                    }
                }
                cachePerformanceBuild.addAll(runningTasks.awaitAll())
            }
            println(
                "Getting cache performance builds in: " + (System.currentTimeMillis().toDuration(DurationUnit.MILLISECONDS) - duration)
            )
        }
        return cachePerformanceBuild
    }
}
