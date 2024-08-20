package io.github.cdsap.geapi.client.model

data class ScanWithAttributes(
    val id: String,
    val projectName: String,
    val requestedTasksGoals: Array<String>,
    val tags: Array<String>,
    val hasFailed: Boolean,
    val environment: Environment,
    val buildDuration: Long,
    val buildTool: String,
    val buildStartTime: Long,
    val values: Array<CustomValue>,
)
