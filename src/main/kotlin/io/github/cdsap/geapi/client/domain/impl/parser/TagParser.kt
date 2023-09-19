package io.github.cdsap.geapi.client.domain.impl.parser

class TagParser {
    fun tagIsIncluded(filterTags: List<String>, buildTags: List<String>, exclusiveTags: Boolean): Boolean {
        if (filterTags.isEmpty()) {
            return true
        }
        if (exclusiveTags) {
            val count = filterTags.size
            var aux = 0
            buildTags.forEach {
                if (filterTags.map { it.uppercase() }.contains(it.uppercase())) {
                    aux++
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
