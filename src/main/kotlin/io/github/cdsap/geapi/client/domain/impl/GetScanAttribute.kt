package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.domain.impl.logger.Logger
import io.github.cdsap.geapi.client.domain.impl.mapper.ScanMapper
import io.github.cdsap.geapi.client.domain.impl.progress.ProgressFeedback
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.model.GradleScan
import io.github.cdsap.geapi.client.model.MavenScan
import io.github.cdsap.geapi.client.model.Scan
import io.github.cdsap.geapi.client.model.ScanWithAttributes
import io.github.cdsap.geapi.client.repository.GradleEnterpriseRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class GetScanAttribute(private val repository: GradleEnterpriseRepository) {

    suspend fun getScanAttributes(
        buildScans: List<Scan>,
        filter: Filter,
        logger: Logger
    ): List<ScanWithAttributes> {
        logger.log("Getting ${buildScans.size} Build Scans Attributes")

        val duration = System.currentTimeMillis().toDuration(DurationUnit.MILLISECONDS)
        val semaphore = Semaphore(permits = filter.concurrentCalls)
        val progressFeedback = ProgressFeedback(filter.clientType, buildScans.size)
        val scanMapper = ScanMapper()
        val scans = mutableListOf<ScanWithAttributes>()

        progressFeedback.init()

        coroutineScope {
            val runningTasks =
                buildScans.filter { it.buildToolType == "gradle" || it.buildToolType == "maven" }.map { sc ->
                    async {
                        semaphore.acquire()
                        val scan = scanWithAttributes(sc, scanMapper)
                        progressFeedback.update()
                        semaphore.release()
                        scan
                    }
                }
            scans.addAll(runningTasks.awaitAll())
        }
        logBuildScanAttributesInformation(duration, logger)
        return scans
    }
    private suspend fun scanWithAttributes(
        sc: Scan,
        scanMapper: ScanMapper
    ): ScanWithAttributes {
        var gradleScan: GradleScan? = null
        var mavenScan: MavenScan? = null

        if (sc.buildToolType == "gradle") {
            gradleScan = repository.getBuildScanGradleAttribute(sc.id)
        } else {
            mavenScan = repository.getBuildScanMavenAttribute(sc.id)
        }
        return scanMapper.scanWithAttributes(gradleScan, mavenScan)
    }

    private fun logBuildScanAttributesInformation(duration: Duration, logger: Logger) {
        val totalDuration = System.currentTimeMillis().toDuration(DurationUnit.MILLISECONDS) - duration
        logger.log("")
        logger.log("Getting Build attributes in: $totalDuration")
    }
}
