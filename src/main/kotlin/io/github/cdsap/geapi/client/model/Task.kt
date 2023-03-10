package io.github.cdsap.geapi.domain.model

import kotlin.time.Duration

data class Task(
    val taskType: String,
    val taskPath: String,
    val avoidanceOutcome: String,
    val duration: Long,
    val fingerprintingDuration: Long
)


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

