package io.github.cdsap.geapi.client.repository.impl

import io.github.cdsap.geapi.client.model.Filter

class FilterBuildScanAdvancedSearch {
    fun filter(filter: Filter): String {
        val filterClauses = buildList {
            filter.project?.let { add("project:$it") }
            if (filter.tags.isNotEmpty()) {
                add(
                    if (filter.exclusiveTags || filter.tags.any { it[0].toString() == "!" }) {
                        returnTagQuery(filter, "AND")
                    } else {
                        returnTagQuery(filter, "OR")
                    },
                )
            }
            if (filter.includeFailedBuilds == false) {
                add("buildOutcome:succeeded")
            }
            filter.user?.let { add("user:$it") }
            filter.requestedTask?.let {
                val escapeColons = it.toString().replace(":", "\\:")
                add("requested:$escapeColons")
            }
            filter.advancedQuery
                ?.takeUnless { it.isBlank() }
                ?.let { add(encodeSpaces(it)) }
        }

        return filterClauses.joinToString("%20")
    }

    private fun encodeSpaces(value: String): String {
        return value.trim().replace(Regex("\\s+"), "%20")
    }

    private fun returnTagQuery(
        filter: Filter,
        operand: String,
    ): String {
        var tag = ""
        filter.tags.forEach {
            if (it.contains("!")) {
                tag += "-tag:\"${it.replaceFirst("!","")}\"%20$operand%20"
            } else {
                tag += "tag:\"$it\"%20$operand%20"
            }
        }
        val last = tag.lastIndexOf("%20$operand%20")
        val filtered = tag.substring(0, last)
        return "($filtered)"
    }
}
