package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.domain.GetBuildsArtifactTransforms
import io.github.cdsap.geapi.client.domain.impl.concurrency.BoundedRequestExecutor
import io.github.cdsap.geapi.client.domain.impl.logger.Logger
import io.github.cdsap.geapi.client.domain.impl.progress.ProgressFeedback
import io.github.cdsap.geapi.client.model.ArtifactTransform
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.model.ScanWithAttributes
import io.github.cdsap.geapi.client.repository.GradleEnterpriseRepository
import java.lang.NullPointerException
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class GetBuildsWithArtifactTransformRequest(private val repository: GradleEnterpriseRepository) :
    GetBuildsArtifactTransforms {
    override suspend fun get(
        builds: List<ScanWithAttributes>,
        filter: Filter,
    ): List<ArtifactTransform> {
        return if (builds.isNotEmpty()) {
            artifactTransform(builds, filter, Logger(filter.clientType))
        } else {
            emptyList()
        }
    }

    private suspend fun artifactTransform(
        builds: List<ScanWithAttributes>,
        filter: Filter,
        logger: Logger,
    ): List<ArtifactTransform> {
        logger.log("Processing artifact transforms for ${builds.size} builds")

        val duration = System.currentTimeMillis().toDuration(DurationUnit.MILLISECONDS)
        val progressFeedback = ProgressFeedback(filter.clientType, builds.size)

        progressFeedback.init()

        val transforms =
            BoundedRequestExecutor.execute(
                items = builds,
                concurrentCalls = filter.concurrentCallsConservative,
            ) {
                val scanId = it.id
                try {
                    val artifactTransform =
                        repository.getArtifactTransformRequest(scanId).artifactTransformExecutions
                    artifactTransform.map { transform -> transform.buildScanId = scanId }
                    progressFeedback.update()
                    artifactTransform
                } catch (exception: NullPointerException) {
                    progressFeedback.update()
                    emptyArray<ArtifactTransform>()
                }
            }
        logger.log(
            "Getting artifact transforms builds in: " + (
                System.currentTimeMillis()
                    .toDuration(DurationUnit.MILLISECONDS) - duration
            ),
        )
        return transforms.filter { it != null }.flatMap { it.toList() }
    }
}
