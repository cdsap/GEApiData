package io.github.cdsap.geapi.client.model

data class MavenScan(
    val id: String,
    val topLevelProjectName: String,
    val requestedGoals: Array<String>,
    val tags: Array<String>,
    val hasFailed: Boolean,
    val environment: Environment,
    val buildDuration: Long,
    val buildStartTime: Long,
    val values: Array<CustomValue>,
)
