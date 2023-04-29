package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.domain.GetBuildScansWithFilter
import io.github.cdsap.geapi.client.model.*
import io.github.cdsap.geapi.client.repository.GradleEnterpriseRepository
import io.github.cdsap.geapi.progressbar.ProgressBar
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class GetBuildScansWithQueryImpl(private val repository: GradleEnterpriseRepository) : GetBuildScansWithFilter {

    override suspend fun get(filter: Filter): List<ScanWithAttributes> {

        val semaphore = Semaphore(permits = filter.concurrentCalls)
        val duration = System.currentTimeMillis().toDuration(DurationUnit.MILLISECONDS)
        val progressBar = ProgressBar()
        println("Calculating Build Scans to retrieve")
        val buildToProcess = (if (filter.maxBuilds < 1000) 1000 else filter.maxBuilds) / 1000
        progressBar.update(0, buildToProcess)
        var i = 0

        val buildScans = mutableListOf<Scan>()
        while (buildScans.size < filter.maxBuilds) {
            val scans = if (buildScans.size == 0) {
                repository.getBuildScans(filter)
            } else {
                repository.getBuildScans(filter, buildScans.last().id)
            }
            progressBar.update(i++, buildToProcess)
            if (buildScans.size + scans.size > filter.maxBuilds) {
                val diff = filter.maxBuilds - buildScans.size
                buildScans.addAll(scans.dropLast(1000 - diff))
            } else {
                buildScans.addAll(scans)
            }
        }
        val scans = mutableListOf<ScanWithAttributes>()
        if (buildScans.isNotEmpty()) {
            println("Date first Build scan processed: ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date(buildScans.first().availableAt))}")
            println("Date last Build scan processed: ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date(buildScans.last().availableAt))}")
            println("Getting ${buildScans.size} Build Scans Attributes")
            val progressBar = ProgressBar()
            progressBar.update(0, buildScans.size)
            var i = 0

            coroutineScope {
                val runningTasks = buildScans.map { sc ->
                    async {
                        semaphore.acquire()
                        var gradleScan: ScanWithAttributesGradle? = null
                        var mavenScan: ScanWithAttributesMaven? = null

                        if (sc.buildToolType == "gradle") {
                            gradleScan = repository.getBuildScanGradleAttribute(sc.id)
                        } else {
                            mavenScan = repository.getBuildScanMavenAttribute(sc.id)
                        }
                        val scan: ScanWithAttributes = scanWithAttributes(gradleScan, mavenScan)
                        progressBar.update(i++, buildScans.size)
                        semaphore.release()
                        scan
                    }
                }
                scans.addAll(runningTasks.awaitAll())
            }
            println(
                "Getting Build attributes in: " + (System.currentTimeMillis()
                    .toDuration(DurationUnit.MILLISECONDS) - duration)
            )
            println("Filtering Build Scans")
            return scans.filter {
                filterBuildScans(it, filter)
            }
        } else {
            return emptyList()
        }
    }

    private fun scanWithAttributes(
        gradleScan: ScanWithAttributesGradle?,
        mavenScan: ScanWithAttributesMaven?
    ): ScanWithAttributes {
        return if (gradleScan != null) {
            ScanWithAttributes(
                buildTool = "gradle",
                id = gradleScan.id,
                projectName = gradleScan.rootProjectName ?: "",
                requestedTasksGoals = gradleScan.requestedTasks,
                tags = gradleScan.tags,
                hasFailed = gradleScan.hasFailed,
                environment = gradleScan.environment,
                buildDuration = gradleScan.buildDuration,
                buildStartTime = gradleScan.buildStartTime,
                values = gradleScan.values
            )
        } else {
            ScanWithAttributes(
                buildTool = "maven",
                id = mavenScan!!.id,
                projectName = mavenScan.topLevelProjectName ?: "",
                requestedTasksGoals = mavenScan.requestedGoals,
                tags = mavenScan.tags,
                hasFailed = mavenScan.hasFailed,
                environment = mavenScan.environment,
                buildDuration = mavenScan.buildDuration,
                buildStartTime = mavenScan.buildStartTime,
                values = mavenScan.values
            )
        }

    }


    private fun filterBuildScans(scanWithAttributes: ScanWithAttributes, filter: Filter): Boolean {
        val filterProcessBuildScansFailed = if (filter.includeFailedBuilds) true else !scanWithAttributes.hasFailed
        val filterProject =
            if (filter.project == null) true else filter.project == scanWithAttributes.projectName
        val filterTags = tagIsIncluded(filter.tags, scanWithAttributes.tags.toList())
        val filterTasks = if (filter.requestedTask == null) true else requestedTasksIncludeTask(
            scanWithAttributes.requestedTasksGoals,
            filter.requestedTask!!
        )
        val filterUser = if (filter.user == null) true else scanWithAttributes.environment.username == filter.user
        return filterProcessBuildScansFailed && filterProject && filterTags && filterTasks && filterUser
    }

    private fun tagIsIncluded(filterTags: List<String>, buildTags: List<String>): Boolean {
        if (filterTags.isEmpty()) {
            return true
        }
        buildTags.forEach {
            if (filterTags.map { it.uppercase() }.contains(it.uppercase())) {
                return true
            }
        }
        return false
    }

    private fun requestedTasksIncludeTask(requestedTasks: Array<String>, task: String): Boolean {
        requestedTasks.forEach {
            if (it.contains(task)) return true
        }
        return false
    }
}
