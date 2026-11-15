package io.github.cdsap.geapi.client.repository.impl

import io.github.cdsap.geapi.client.model.Filter

internal class BuildScanQueryParameters {
    fun build(
        filter: Filter,
        buildId: String?,
        query: String? = null,
    ): String {
        val maxBuilds =
            if (filter.maxBuilds < 1000) {
                filter.maxBuilds
            } else {
                1000
            }
        val parameters =
            listOfNotNull(
                buildId?.let { "fromBuild=$it" }
                    ?: filter.fromInstant?.let { "fromInstant=$it" },
                "maxBuilds=$maxBuilds",
                "reverse=true",
                query?.let { "query=$it" },
            )
        return parameters.joinToString("&")
    }
}
