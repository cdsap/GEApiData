package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.domain.GetBuildProfile
import io.github.cdsap.geapi.client.domain.impl.logger.Logger
import io.github.cdsap.geapi.client.model.BuildProfileOverview
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.model.ScanWithAttributes
import io.github.cdsap.geapi.client.repository.GradleEnterpriseRepository
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

        val duration = System.currentTimeMillis().toDuration(DurationUnit.MILLISECONDS)
        val buildProfile =
            BuildScanBatchProcessor.process(
                builds = builds,
                filter = filter,
                predicate = { it.buildTool == "gradle" },
            ) { scanAttributes ->
                repository.getBuildProfileOverview(scanAttributes.id).also { profile ->
                    profile.id = scanAttributes.id
                }
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
