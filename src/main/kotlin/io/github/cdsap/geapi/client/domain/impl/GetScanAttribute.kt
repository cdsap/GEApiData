package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.domain.impl.concurrency.BoundedRequestExecutor
import io.github.cdsap.geapi.client.domain.impl.logger.Logger
import io.github.cdsap.geapi.client.domain.impl.mapper.ScanMapper
import io.github.cdsap.geapi.client.domain.impl.progress.ProgressFeedback
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.model.Scan
import io.github.cdsap.geapi.client.model.ScanWithAttributes
import io.github.cdsap.geapi.client.repository.BuildScanAttributesRepository
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class GetScanAttribute(private val repository: BuildScanAttributesRepository) {
    suspend fun getScanAttributes(
        buildScans: List<Scan>,
        filter: Filter,
        logger: Logger,
    ): List<ScanWithAttributes> {
        logger.log("Getting ${buildScans.size} Build Scans Attributes")

        val duration = System.currentTimeMillis().toDuration(DurationUnit.MILLISECONDS)
        val progressFeedback = ProgressFeedback(filter.clientType, buildScans.size)
        val scanMapper = ScanMapper()

        progressFeedback.init()

        val scans =
            BoundedRequestExecutor.execute(
                items = buildScans.filter { it.buildToolType == "gradle" || it.buildToolType == "maven" },
                concurrentCalls = filter.concurrentCalls,
            ) { sc ->
                val scan = scanWithAttributes(sc, scanMapper)
                progressFeedback.update()
                scan
            }
        logBuildScanAttributesInformation(duration, logger)
        return scans
    }

    private suspend fun scanWithAttributes(
        sc: Scan,
        scanMapper: ScanMapper,
    ): ScanWithAttributes {
        return if (sc.buildToolType == "gradle") {
            scanMapper.scanWithAttributes(repository.getBuildScanGradleAttribute(sc.id))
        } else {
            scanMapper.scanWithAttributes(repository.getBuildScanMavenAttribute(sc.id))
        }
    }

    private fun logBuildScanAttributesInformation(
        duration: Duration,
        logger: Logger,
    ) {
        val totalDuration = System.currentTimeMillis().toDuration(DurationUnit.MILLISECONDS) - duration
        logger.log("")
        logger.log("Getting Build attributes in: $totalDuration")
    }
}
