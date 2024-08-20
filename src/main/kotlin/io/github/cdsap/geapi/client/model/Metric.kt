package io.github.cdsap.geapi.client.model

data class Metric(
    val max: Long,
    val average: Long,
    val median: Long,
    val p25: Long,
    val p75: Long,
    val p95: Long,
)
