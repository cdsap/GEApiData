package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.domain.GetBuildsWithAttributes
import io.github.cdsap.geapi.client.domain.impl.filter.FilterBuildScan
import io.github.cdsap.geapi.client.domain.impl.logger.Logger
import io.github.cdsap.geapi.client.domain.impl.progress.ProgressFeedback
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.model.Scan
import io.github.cdsap.geapi.client.model.ScanWithAttributes
import io.github.cdsap.geapi.client.repository.GradleEnterpriseRepository
import java.text.SimpleDateFormat
import java.util.*

class GetBuildsWithAttributesRequest(private val repository: GradleEnterpriseRepository) : GetBuildsWithAttributes {

    override suspend fun get(filter: Filter): List<ScanWithAttributes> {
        val logger = Logger(filter.clientType)
        val buildScans = aggregateBuildScansToRetrieve(filter, logger)
        return if (buildScans.isNotEmpty()) {
            val filterBuildScan = FilterBuildScan()
            logger.log("Filtering Build Scans")
            return GetScanAttribute(repository).getScanAttributes(buildScans, filter, logger).filter { filterBuildScan.filter(it, filter) }
        } else {
            emptyList()
        }
    }

    private suspend fun aggregateBuildScansToRetrieve(
        filter: Filter,
        logger: Logger
    ): List<Scan> {
        logger.log("Calculating Build Scans to retrieve")
        val buildToProcess = (if (filter.maxBuilds < 1000) 1000 else filter.maxBuilds) / 1000
        val progressFeedback = ProgressFeedback(filter.clientType, buildToProcess)
        val buildScans = mutableListOf<Scan>()

        progressFeedback.init()

        while (buildScans.size < filter.maxBuilds) {
            val scans = if (buildScans.size == 0) {
                if (filter.sinceBuildId != null) {
                    repository.getBuildScans(filter, filter.sinceBuildId)
                } else {
                    repository.getBuildScans(filter)
                }
            } else {
                repository.getBuildScans(filter, buildScans.last().id)
            }
            progressFeedback.update()
            if (buildScans.size + scans.size > filter.maxBuilds) {
                val diff = filter.maxBuilds - buildScans.size
                buildScans.addAll(scans.dropLast(1000 - diff))
            } else {
                buildScans.addAll(scans)
            }
        }
        logBuildScanInformation(buildScans, logger)
        return buildScans
    }

    private fun logBuildScanInformation(buildScans: List<Scan>, logger: Logger) {
        if (buildScans.isNotEmpty()) {
            val dateInit = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date(buildScans.first().availableAt))
            logger.log("")
            logger.log("Date first Build scan processed: $dateInit")
            val dateEnd = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date(buildScans.last().availableAt))
            logger.log("Date last Build scan processed: $dateEnd")
        } else {
            logger.log("")
            logger.log("No build scans found")
        }
    }
}
