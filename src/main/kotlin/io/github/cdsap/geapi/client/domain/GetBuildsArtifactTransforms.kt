package io.github.cdsap.geapi.client.domain

import io.github.cdsap.geapi.client.model.ArtifactTransform
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.model.ScanWithAttributes

interface GetBuildsArtifactTransforms {
    suspend fun get(builds: List<ScanWithAttributes>, filter: Filter): List<ArtifactTransform>
}
