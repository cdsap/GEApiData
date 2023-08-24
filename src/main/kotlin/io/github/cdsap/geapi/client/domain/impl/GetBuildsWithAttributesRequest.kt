package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.domain.GetBuildsWithAttributes
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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class GetBuildsWithAttributesRequest(private val repository: GradleEnterpriseRepository) : GetBuildsWithAttributes {

    override suspend fun get(filter: Filter): List<ScanWithAttributes> {
        val logger = Logger(filter.clientType)
        val buildScans = aggregateBuildScansToRetrieve(filter, logger)
        return if (buildScans.isNotEmpty()) {
            val filterBuildScan = FilterBuildScan()
            logger.log("Filtering Build Scans")
            return getScanAttributes(buildScans, filter, logger).filter { filterBuildScan.filter(it, filter) }
        } else {
            emptyList()
        }
    }

    private suspend fun getScanAttributes(
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

    private fun logBuildScanInformation(buildScans: List<Scan>, logger: Logger) {
        val dateInit = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date(buildScans.first().availableAt))
        logger.log("")
        logger.log("Date first Build scan processed: $dateInit")
        val dateEnd = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date(buildScans.last().availableAt))
        logger.log("Date last Build scan processed: $dateEnd")
    }
}
