package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.domain.GetArtifactTransforms
import io.github.cdsap.geapi.client.model.ArtifactTransform
import io.github.cdsap.geapi.client.repository.GradleEnterpriseRepository
import java.lang.NullPointerException

class GetSingleBuildArtifactTransformRequest(private val repository: GradleEnterpriseRepository) :
    GetArtifactTransforms {

    override suspend fun get(buildId: String): List<ArtifactTransform> {
        return artifactTransform(buildId)
    }

    private suspend fun artifactTransform(
        buildId: String
    ): List<ArtifactTransform> {
        try {
            val artifactTransform = repository.getArtifactTransformRequest(buildId)
            artifactTransform.artifactTransformExecutions.map { it.buildScanId = buildId }
            return artifactTransform.artifactTransformExecutions.toList()
        } catch (e: NullPointerException) {
            return emptyList()
        }
    }
}
