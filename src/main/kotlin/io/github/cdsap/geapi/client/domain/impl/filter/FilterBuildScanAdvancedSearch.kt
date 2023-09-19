package io.github.cdsap.geapi.client.domain.impl.filter

import io.github.cdsap.geapi.client.model.Filter

class FilterBuildScanAdvancedSearch {

    fun filter(filter: Filter): String {
        var filterIncludedBuildValue = ""
        if (filter.includeFailedBuilds != null) {
            if (!filter.includeFailedBuilds) {
                filterIncludedBuildValue = "buildOutcome:succeeded"
            }
        }

        var filterProjectValue = ""
        if (filter.project != null) {
            filterProjectValue = "project:${filter.project}"
        }
        var filterTagsValue = ""
        if (filter.tags.isNotEmpty()) {
            filterTagsValue = if (filter.exclusiveTags) {
                returnTagQuery(filter, "AND")
            } else {
                returnTagQuery(filter, "OR")
            }
        }

        var filterRequestedTasks = ""
        if (filter.requestedTask != null) {
            filterRequestedTasks = "requested:${filter.requestedTask}"
        }

        var filterUser = ""
        if (filter.user != null) {
            filterUser = "user:${filter.user}"
        }

        var queryString = ""
        if (filterProjectValue.isNotEmpty()) {
            queryString += insertValue(filterProjectValue, queryString)
        }
        if (filterTagsValue.isNotEmpty()) {
            queryString += insertValue(filterTagsValue, queryString)
        }
        if (filterIncludedBuildValue.isNotEmpty()) {
            queryString += insertValue(filterIncludedBuildValue, queryString)
        }
        if (filterUser.isNotEmpty()) {
            queryString += insertValue(filterUser, queryString)
        }
        if (filterRequestedTasks.isNotEmpty()) {
            queryString += insertValue(filterRequestedTasks, queryString)
        }
        return queryString
    }

    private fun insertValue(value: String, queryString: String): String {
        return if (queryString.isEmpty()) {
            value
        } else {
            "%20$value"
        }
    }

    private fun returnTagQuery(filter: Filter, operand: String): String {
        var tag = ""
        filter.tags.forEach {
            tag += "tag:$it%20$operand%20"
        }
        val last = tag.lastIndexOf("%20$operand%20")
        val filtered = tag.substring(0, last)
        return "($filtered)"
    }
}
