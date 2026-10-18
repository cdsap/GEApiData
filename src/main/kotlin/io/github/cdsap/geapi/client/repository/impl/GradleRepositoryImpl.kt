package io.github.cdsap.geapi.client.repository.impl

import io.github.cdsap.geapi.client.model.ArtifactTransforms
import io.github.cdsap.geapi.client.model.Build
import io.github.cdsap.geapi.client.model.BuildProfileOverview
import io.github.cdsap.geapi.client.model.BuildWithResourceUsage
import io.github.cdsap.geapi.client.model.ConfigurationCacheResult
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.model.GradleScan
import io.github.cdsap.geapi.client.model.MavenScan
import io.github.cdsap.geapi.client.model.Scan
import io.github.cdsap.geapi.client.network.GEClient
import io.github.cdsap.geapi.client.repository.GradleEnterpriseRepository

class GradleRepositoryImpl(private val client: GEClient) : GradleEnterpriseRepository {
    override suspend fun getBuildScans(
        filter: Filter,
        buildId: String?,
    ): Array<Scan> {
        return client.get(buildBuildScansUrl(filter, buildId))
    }

    override suspend fun getBuildScanGradleAttribute(id: String): GradleScan {
        return client.get("${client.url}/$id/gradle-attributes")
    }

    override suspend fun getBuildScanMavenAttribute(id: String): MavenScan {
        return client.get("${client.url}/$id/maven-attributes")
    }

    override suspend fun getBuildScanGradleCachePerformance(id: String): Build {
        return client.get("${client.url}/$id/gradle-build-cache-performance")
    }

    override suspend fun getBuildScanMavenCachePerformance(id: String): Build {
        return client.get("${client.url}/$id/maven-build-cache-performance")
    }

    override suspend fun getArtifactTransformRequest(id: String): ArtifactTransforms {
        return client.get("${client.url}/$id/gradle-artifact-transform-executions")
    }

    override suspend fun getBuildScanGradlePerformance(id: String): BuildWithResourceUsage {
        return client.get("${client.url}/$id/gradle-resource-usage")
    }

    override suspend fun getConfigurationCacheResult(id: String): ConfigurationCacheResult {
        return client.get("${client.url}/$id/gradle-configuration-cache")
    }

    override suspend fun getBuildProfileOverview(id: String): BuildProfileOverview {
        return client.get("${client.url}/$id/gradle-build-profile-overview")
    }

    override suspend fun getBuildScansWithAdvancedQuery(
        filter: Filter,
        query: String,
        buildId: String?,
    ): Array<Scan> {
        return client.get(buildBuildScansUrl(filter, buildId, query))
    }

    private fun buildBuildScansUrl(
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
                buildId?.let { "fromBuild=$it" },
                "maxBuilds=$maxBuilds",
                "reverse=true",
                query?.let { "query=$it" },
            )
        return "${client.url}?${parameters.joinToString("&")}"
    }
}
