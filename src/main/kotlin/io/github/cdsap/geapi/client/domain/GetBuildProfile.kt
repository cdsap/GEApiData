package io.github.cdsap.geapi.client.domain

import io.github.cdsap.geapi.client.model.BuildProfileOverview
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.model.ScanWithAttributes

interface GetBuildProfile {
    suspend fun get(
        builds: List<ScanWithAttributes>,
        filter: Filter,
    ): List<BuildProfileOverview>
}
