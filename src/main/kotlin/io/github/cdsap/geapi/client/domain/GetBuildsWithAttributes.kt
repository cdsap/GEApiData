package io.github.cdsap.geapi.client.domain

import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.model.ScanWithAttributes

interface GetBuildsWithAttributes {
    suspend fun get(filter: Filter = Filter()): List<ScanWithAttributes>
}
