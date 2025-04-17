package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.domain.GetBuildProfile
import io.github.cdsap.geapi.client.domain.impl.logger.Logger
import io.github.cdsap.geapi.client.domain.impl.progress.ProgressFeedback
import io.github.cdsap.geapi.client.model.BuildProfileOverview
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.model.ScanWithAttributes
import io.github.cdsap.geapi.client.repository.GradleEnterpriseRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class GetBuildsProfileRequest(private val repository: GradleEnterpriseRepository) : GetBuildProfile {
    override suspend fun get(
        builds: List<ScanWithAttributes>,
        filter: Filter,
    ): List<BuildProfileOverview> {
        return if (builds.isNotEmpty()) {
            processProfile(builds, filter, Logger(filter.clientType))
        } else {
            emptyList()
        }
    }

    private suspend fun processProfile(
        builds: List<ScanWithAttributes>,
        filter: Filter,
        logger: Logger,
    ): List<BuildProfileOverview> {
        logger.log("Processing build scan profile for ${builds.size} builds")

        val buildProfile = mutableListOf<BuildProfileOverview>()
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

                        val profile =
                            repository.getBuildProfileOverview(scanAttributes.id)
                        profile.id = scanAttributes.id
                        progressFeedback.update()
                        semaphore.release()
                        profile
                    }
                }
            buildProfile.addAll(runningTasks.awaitAll())
        }
        logger.log(
            "Getting profile builds in: " + (
                System.currentTimeMillis()
                    .toDuration(DurationUnit.MILLISECONDS) - duration
            ),
        )
        return buildProfile
    }
}
