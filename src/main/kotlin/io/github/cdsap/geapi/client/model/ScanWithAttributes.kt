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
    val buildStartTime: Long
)

data class ScanWithAttributesGradle(
    val id: String,
    val rootProjectName: String,
    val requestedTasks: Array<String>,
    val tags: Array<String>,
    val hasFailed: Boolean,
    val environment: Environment,
    val buildDuration: Long,
    val buildStartTime: Long
)

data class ScanWithAttributesMaven(
    val id: String,
    val topLevelProjectName: String,
    val requestedGoals: Array<String>,
    val tags: Array<String>,
    val hasFailed: Boolean,
    val environment: Environment,
    val buildDuration: Long,
    val buildStartTime: Long
)
