package io.github.cdsap.geapi.client.model

data class PerformanceUsage(
    val totalMemory: Long,
    val total: PerformanceMetrics,
    val nonExecution: PerformanceMetrics,
    val execution: PerformanceMetrics,
    var builtTool: String,
    var tags: Array<String> = emptyArray(),
    var requestedTask: Array<String> = emptyArray(),
    var id: String = "",
    var buildDuration: Long = 0L,
    var buildStartTime: Long = 0L,
    var projectName: String = "",
    var environment: Environment,
    var values: Array<CustomValue> = emptyArray(),
)
