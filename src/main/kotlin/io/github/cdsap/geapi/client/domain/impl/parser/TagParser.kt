package io.github.cdsap.geapi.client.domain.impl.parser

class TagParser {
    fun tagIsIncluded(
        filterTags: List<String>,
        buildTags: List<String>,
        exclusiveTags: Boolean,
    ): Boolean {
        if (filterTags.isEmpty()) {
            return true
        }
        val isExclusive = exclusiveTags || filterTags.any { it[0].toString() == "!" }
        if (isExclusive) {
            val count = filterTags.size
            var aux = 0
            filterTags.forEach {
                if (it[0].toString() == "!") {
                    val newTag = it.replaceFirst("!", "").uppercase()
                    if (!buildTags.map { it.uppercase() }.contains(newTag)) {
                        aux++
                    }
                } else {
                    if (buildTags.map { it.uppercase() }
                            .contains(it.uppercase())
                    ) {
                        aux++
                    }
                }
            }
            return count == aux
        } else {
            buildTags.forEach {
                if (filterTags.map { it.uppercase() }.contains(it.uppercase())) {
                    return true
                }
            }
        }
        return false
    }
}
