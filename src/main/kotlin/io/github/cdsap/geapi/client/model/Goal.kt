package io.github.cdsap.geapi.client.model

data class Goal(
    val goalName: String,
    val mojoType: String,
    val goalExecutionId: String,
    val goalProjectName: String,
    val avoidanceOutcome: String,
    val duration: Long,
    val nonCacheabilityCategory: String,
    val nonCacheabilityReason: String,
    val fingerprintingDuration: Long
)
