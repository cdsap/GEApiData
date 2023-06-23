package io.github.cdsap.geapi.client.model

data class Filter(
    val url: String,
    var maxBuilds: Int = 50,
    var range: Long? = null,
    var sinceBuildId: String? = null,
    val project: String? = null,
    val includeFailedBuilds: Boolean = false,
    var requestedTask: String? = null,
    var tags: List<String> = emptyList(),
    var taskType: String? = null,
    val initFilter: Long,
    val since: Long? = null,
    val user: String? = null,
    val experimentId: String? = null,
    val buildSystem: String = "gradle",
    val concurrentCalls: Int = 10,
    val concurrentCallsConservative: Int,
    val variants: String? = null,
    val maxDuration: Long = 0L,
    val exclusiveTags: Boolean = false
)
