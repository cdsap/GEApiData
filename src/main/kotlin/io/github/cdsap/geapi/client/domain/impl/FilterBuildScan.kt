package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.model.ScanWithAttributes

class FilterBuildScan {

    fun filter(scanWithAttributes: ScanWithAttributes, filter: Filter): Boolean {
        val filterProcessBuildScansFailed = if (filter.includeFailedBuilds) true else !scanWithAttributes.hasFailed
        val filterProject =
            if (filter.project == null) true else filter.project == scanWithAttributes.projectName
        val filterTags = TagParser().tagIsIncluded(filter.tags, scanWithAttributes.tags.toList(), filter.exclusiveTags)
        val filterTasks = if (filter.requestedTask == null) {
            true
        } else {
            requestedTasksIncludeTask(
                scanWithAttributes.requestedTasksGoals,
                filter.requestedTask!!
            )
        }
        val filterUser = if (filter.user == null) true else scanWithAttributes.environment.username == filter.user
        return filterProcessBuildScansFailed && filterProject && filterTags && filterTasks && filterUser
    }

    private fun requestedTasksIncludeTask(requestedTasks: Array<String>, task: String): Boolean {
        requestedTasks.forEach {
            if (it.contains(task)) return true
        }
        return false
    }
}
