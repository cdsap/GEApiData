package io.github.cdsap.geapi.client.repository

import io.github.cdsap.geapi.client.model.GradleScan
import io.github.cdsap.geapi.client.model.MavenScan

interface BuildScanAttributesRepository {
    suspend fun getBuildScanGradleAttribute(id: String): GradleScan

    suspend fun getBuildScanMavenAttribute(id: String): MavenScan
}
