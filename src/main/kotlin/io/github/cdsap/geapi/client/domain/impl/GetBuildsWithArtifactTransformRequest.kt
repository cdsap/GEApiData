package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.domain.GetBuildsArtifactTransforms
import io.github.cdsap.geapi.client.domain.impl.logger.Logger
import io.github.cdsap.geapi.client.domain.impl.progress.ProgressFeedback
import io.github.cdsap.geapi.client.model.ArtifactTransform
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.model.ScanWithAttributes
import io.github.cdsap.geapi.client.repository.GradleEnterpriseRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import java.lang.NullPointerException
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class GetBuildsWithArtifactTransformRequest(private val repository: GradleEnterpriseRepository) :
    GetBuildsArtifactTransforms {

    override suspend fun get(builds: List<ScanWithAttributes>, filter: Filter): List<ArtifactTransform> {
        return if (builds.isNotEmpty()) {
            artifactTransform(builds, filter, Logger(filter.clientType))
        } else {
            emptyList()
        }
    }

    private suspend fun artifactTransform(
        builds: List<ScanWithAttributes>,
        filter: Filter,
        logger: Logger
    ): List<ArtifactTransform> {
        logger.log("Processing artifact transforms for ${builds.size} builds")

        val transforms = mutableListOf<Array<ArtifactTransform>>()
        val duration = System.currentTimeMillis().toDuration(DurationUnit.MILLISECONDS)
        val progressFeedback = ProgressFeedback(filter.clientType, builds.size)
        val semaphore = Semaphore(filter.concurrentCallsConservative)

        progressFeedback.init()

        coroutineScope {
            val runningTasks = builds.map {
                async {
                    semaphore.acquire()
                    val scanId = it.id
                    try {
                        val artifactTransform =
                            repository.getArtifactTransformRequest(scanId).artifactTransformExecutions
                        artifactTransform.map { it.buildScanId = scanId }
                        progressFeedback.update()
                        semaphore.release()
                        artifactTransform
                    } catch (exception: NullPointerException) {
                        progressFeedback.update()
                        semaphore.release()
                        emptyArray<ArtifactTransform>()
                    }
                }
            }
            transforms.addAll(runningTasks.awaitAll())
        }
        logger.log(
            "Getting artifact transforms builds in: " + (
                System.currentTimeMillis()
                    .toDuration(DurationUnit.MILLISECONDS) - duration
                )
        )
        return transforms.filter { it != null }.flatMap { it.toList() }
    }
}
