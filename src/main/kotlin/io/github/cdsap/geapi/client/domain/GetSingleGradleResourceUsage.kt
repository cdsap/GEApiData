package io.github.cdsap.geapi.client.domain

import io.github.cdsap.geapi.client.model.BuildWithResourceUsage

interface GetSingleGradleResourceUsage {
    suspend fun get(buildId: String): BuildWithResourceUsage
}
