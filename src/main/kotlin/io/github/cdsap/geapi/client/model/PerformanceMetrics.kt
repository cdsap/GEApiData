package io.github.cdsap.geapi.client.model

data class PerformanceMetrics(
    val allProcessesCpu: Metric,
    val buildProcessCpu: Metric,
    val buildChildProcessesCpu: Metric,
    val allProcessesMemory: Metric,
    val buildProcessMemory: Metric,
    val buildChildProcessesMemory: Metric,
    val diskReadThroughput: Metric,
    val diskWriteThroughput: Metric,
    val networkUploadThroughput: Metric,
    val networkDownloadThroughput: Metric,
)
