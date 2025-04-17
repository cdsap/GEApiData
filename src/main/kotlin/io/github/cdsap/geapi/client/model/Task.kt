package io.github.cdsap.geapi.client.model

data class Task(
    val taskType: String,
    val taskPath: String,
    val avoidanceOutcome: String,
    val duration: Long,
    val fingerprintingDuration: Long,
    val cacheArtifactSize: Long? = null,
    val cacheKey: String? = null,
)
