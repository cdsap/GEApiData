package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.model.ArtifactTransforms
import io.github.cdsap.geapi.client.model.Build
import io.github.cdsap.geapi.client.model.BuildWithResourceUsage
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.model.GradleScan
import io.github.cdsap.geapi.client.model.MavenScan
import io.github.cdsap.geapi.client.model.Scan
import io.github.cdsap.geapi.client.repository.GradleEnterpriseRepository

open class FakeTestRepository : GradleEnterpriseRepository {
    override suspend fun getBuildScans(
        filter: Filter,
        buildId: String?,
    ): Array<Scan> {
        TODO("Not yet implemented")
    }

    override suspend fun getBuildScansWithAdvancedQuery(
        filter: Filter,
        buildId: String?,
    ): Array<Scan> {
        TODO("Not yet implemented")
    }

    override suspend fun getBuildScanGradleAttribute(id: String): GradleScan {
        TODO("Not yet implemented")
    }

    override suspend fun getBuildScanMavenAttribute(id: String): MavenScan {
        TODO("Not yet implemented")
    }

    override suspend fun getBuildScanGradleCachePerformance(id: String): Build {
        TODO("Not yet implemented")
    }

    override suspend fun getBuildScanMavenCachePerformance(id: String): Build {
        TODO("Not yet implemented")
    }

    override suspend fun getArtifactTransformRequest(id: String): ArtifactTransforms {
        TODO("Not yet implemented")
    }

    override suspend fun getBuildScanGradlePerformance(id: String): BuildWithResourceUsage {
        TODO("Not yet implemented")
    }
}
