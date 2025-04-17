package io.github.cdsap.geapi.client.model

data class BuildProfileOverview(
    var id: String? = null,
    val breakdown: Breakdown,
    val memoryUsage: MemoryUsage,
)
