package io.github.cdsap.geapi.client.model

data class ConfigurationCacheResultResponse(
    val outcome: String,
    val entrySize: Long,
    val store: ConfigurationCacheOperation?,
    val load: ConfigurationCacheOperation?,
    val missReasons: List<String> = emptyList(),
)
