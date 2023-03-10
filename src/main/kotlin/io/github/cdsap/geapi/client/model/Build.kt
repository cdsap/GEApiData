package io.github.cdsap.geapi.client.model

import io.github.cdsap.geapi.domain.model.Goal
import io.github.cdsap.geapi.domain.model.Task

data class Build(
    var builtTool: String,
    val taskExecution: Array<Task>,
    var tags: Array<String> = emptyArray(),
    var requestedTask: Array<String> = emptyArray(),
    var id: String = "",
    var buildDuration: Long = 0L,
    var experiment: Experiment = Experiment.VARIANT_A,
    var OS: OS = io.github.cdsap.geapi.client.model.OS.MAC,
    val metrics: MutableMap<String, Any>,
    val avoidanceSavingsSummary: AvoidanceSavingsSummary,
    val taskFingerprintingSummary: TaskFingerprintingSummary,
    var buildStartTime: Long = 0L,
    var projectName: String = "",
    val goalExecution: Array<Goal>,
    val goalFingerprintingSummary: TaskFingerprintingSummary,
)

enum class Experiment {
    VARIANT_A,
    VARIANT_B
}

enum class OS {
    MAC,
    Linux
}

data class TaskFingerprintingSummary(val count: Int, val serialDuration: Long)
