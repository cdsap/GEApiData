package io.github.cdsap.geapi.client.repository

import io.github.cdsap.geapi.client.model.BuildProfileOverview

interface BuildProfileRepository {
    suspend fun getBuildProfileOverview(id: String): BuildProfileOverview
}
