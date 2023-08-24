package io.github.cdsap.geapi.client.model

data class GradleScan(
    val id: String,
    val rootProjectName: String,
    val requestedTasks: Array<String>,
    val tags: Array<String>,
    val hasFailed: Boolean,
    val environment: Environment,
    val buildDuration: Long,
    val buildStartTime: Long,
    val values: Array<CustomValue>
)
