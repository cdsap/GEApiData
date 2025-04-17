package io.github.cdsap.geapi.client.model

data class MemoryUsage(
    val totalGarbageCollectionTime: Long,
    val memoryPools: Array<MemoryPool>,
)
