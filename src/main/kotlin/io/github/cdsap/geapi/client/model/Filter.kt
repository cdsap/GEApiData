package io.github.cdsap.geapi.client.model

data class Filter(
    var maxBuilds: Int = 50,
    var sinceBuildId: String? = null,
    val project: String? = null,
    val includeFailedBuilds: Boolean = true,
    var requestedTask: String? = null,
    var tags: List<String> = emptyList(),
    val user: String? = null,
    val exclusiveTags: Boolean = false,
    val concurrentCalls: Int = 10,
    val concurrentCallsConservative: Int = 5,
    val clientType: ClientType = ClientType.API,
)
