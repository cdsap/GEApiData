package io.github.cdsap.geapi.client.model

data class Build(
    var builtTool: String,
    val taskExecution: Array<Task>,
    var tags: Array<String> = emptyArray(),
    var requestedTask: Array<String> = emptyArray(),
    var id: String = "",
    var buildDuration: Long = 0L,
    val avoidanceSavingsSummary: AvoidanceSavingsSummary,
    var buildStartTime: Long = 0L,
    var projectName: String = "",
    val goalExecution: Array<Goal>,
    var values: Array<CustomValue> = emptyArray()
)
