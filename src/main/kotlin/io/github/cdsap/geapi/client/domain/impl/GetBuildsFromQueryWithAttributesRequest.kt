package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.domain.GetBuildsWithAttributes
import io.github.cdsap.geapi.client.domain.impl.logger.Logger
import io.github.cdsap.geapi.client.domain.impl.progress.ProgressFeedback
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.model.Scan
import io.github.cdsap.geapi.client.model.ScanWithAttributes
import io.github.cdsap.geapi.client.repository.GradleEnterpriseRepository
import java.text.SimpleDateFormat
import java.util.*

class GetBuildsFromQueryWithAttributesRequest(private val repository: GradleEnterpriseRepository) : GetBuildsWithAttributes {

    override suspend fun get(filter: Filter): List<ScanWithAttributes> {
        val logger = Logger(filter.clientType)
        val buildScans = aggregateBuildScansToRetrieve(filter, logger)
        return if (buildScans.isNotEmpty()) {
            return GetScanAttribute(repository).getScanAttributes(buildScans, filter, logger)
        } else {
            emptyList()
        }
    }

    private suspend fun aggregateBuildScansToRetrieve(
        filter: Filter,
        logger: Logger
    ): List<Scan> {
        logger.log("Calculating Build Scans to retrieve")
        val buildToProcess = filter.maxBuilds
        val progressFeedback = ProgressFeedback(filter.clientType, buildToProcess)
        val buildScans = mutableListOf<Scan>()

        progressFeedback.init()

        var continueCalls = true

        while (buildScans.size < filter.maxBuilds && continueCalls) {
            val scans = if (buildScans.size == 0) {
                if (filter.sinceBuildId != null) {
                    repository.getBuildScansWithAdvancedQuery(filter, filter.sinceBuildId)
                } else {
                    repository.getBuildScansWithAdvancedQuery(filter)
                }
            } else {
                repository.getBuildScansWithAdvancedQuery(filter, buildScans.last().id)
            }

            if (buildScans.size + scans.size > filter.maxBuilds) {
                val diff = filter.maxBuilds - buildScans.size
                buildScans.addAll(scans.dropLast(1000 - diff))
            } else {
                buildScans.addAll(scans)
            }
            if (buildScans.size < filter.maxBuilds && filter.maxBuilds <= 1000) {
                continueCalls = false
                progressFeedback.explicitUpdate(filter.maxBuilds)
            } else {
                progressFeedback.explicitUpdate(buildScans.size - 1)
            }
        }
        logBuildScanInformation(buildScans, logger)
        return buildScans
    }

    private fun logBuildScanInformation(buildScans: List<Scan>, logger: Logger) {
        val dateInit = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date(buildScans.first().availableAt))
        logger.log("")
        logger.log("Date first Build scan processed: $dateInit")
        val dateEnd = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date(buildScans.last().availableAt))
        logger.log("Date last Build scan processed: $dateEnd")
    }
}
