package io.github.cdsap.geapi.client.model

data class Breakdown(
    val total: Long,
    val initialization: Long,
    val configuration: Long,
    val execution: Long,
    val endOfBuild: Long,
)
