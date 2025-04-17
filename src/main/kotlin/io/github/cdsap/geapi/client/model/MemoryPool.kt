package io.github.cdsap.geapi.client.model

data class MemoryPool(
    val name: String,
    val peakMemory: Long,
    val maxMemory: Long,
)
