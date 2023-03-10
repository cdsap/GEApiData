package io.github.cdsap.geapi.client.repository.impl

import io.github.cdsap.geapi.client.model.*
import io.github.cdsap.geapi.client.repository.GradleEnterpriseRepository
import io.github.cdsap.geapi.domain.model.*
import io.github.cdsap.geapi.client.network.GEClient

class GradleRepositoryImpl(private val client: GEClient) : GradleEnterpriseRepository {

    override suspend fun getBuildScans(filter: Filter, buildId: String?): Array<Scan> {
        val filtering = if (buildId != null) {
            "fromBuild=$buildId"
        } else {
            ""
        }
        val maxBuilds = if (filter.maxBuilds < 1000) {
            filter.maxBuilds
        } else {
            1000
        }
        return client.get("${client.url}?$filtering&maxBuilds=$maxBuilds&reverse=true")
    }

    override suspend fun getBuildScanGradleAttribute(id: String): ScanWithAttributesGradle {
        return client.get("${client.url}/$id/gradle-attributes")
    }

    override suspend fun getBuildScanMavenAttribute(id: String): ScanWithAttributesMaven {
        return client.get("${client.url}/$id/maven-attributes")
    }

    override suspend fun getBuildScanGradleCachePerformance(id: String): Build {
        return client.get("${client.url}/$id/gradle-build-cache-performance")
    }

    override suspend fun getBuildScanMavenCachePerformance(id: String): Build {
        return client.get("${client.url}/$id/maven-build-cache-performance")
    }
}
