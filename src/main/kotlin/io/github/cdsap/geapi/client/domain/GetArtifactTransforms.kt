package io.github.cdsap.geapi.client.domain

import io.github.cdsap.geapi.client.model.ArtifactTransform

interface GetArtifactTransforms {
    suspend fun get(buildId: String): List<ArtifactTransform>
}
