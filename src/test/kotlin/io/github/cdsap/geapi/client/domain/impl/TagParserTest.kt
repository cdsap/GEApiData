package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.domain.impl.parser.TagParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TagParserTest {

    private val tagParser = TagParser()

    @Test
    fun testTagIsIncluded_withEmptyFilterTags_shouldReturnTrue() {
        val filterTags = emptyList<String>()
        val buildTags = listOf("tag1", "tag2")
        val exclusiveTags = false

        val result = tagParser.tagIsIncluded(filterTags, buildTags, exclusiveTags)

        assertEquals(true, result)
    }

    @Test
    fun testTagIsIncluded_withMatchingTagsAndExclusiveTags_shouldReturnTrue() {
        val filterTags = listOf("tag1", "tag2")
        val buildTags = listOf("tag1", "tag2")
        val exclusiveTags = true

        val result = tagParser.tagIsIncluded(filterTags, buildTags, exclusiveTags)

        assertEquals(true, result)
    }

    @Test
    fun testTagIsIncluded_withNonMatchingTagsAndExclusiveTags_shouldReturnFalse() {
        val filterTags = listOf("tag1", "tag2")
        val buildTags = listOf("tag3")
        val exclusiveTags = true

        val result = tagParser.tagIsIncluded(filterTags, buildTags, exclusiveTags)

        assertEquals(false, result)
    }

    @Test
    fun testTagIsIncluded_withOnlyOneMatchingTagsAndExclusiveTags_shouldReturnFalse() {
        val filterTags = listOf("main", "ci")
        val buildTags = listOf("ci")
        val exclusiveTags = true

        val result = tagParser.tagIsIncluded(filterTags, buildTags, exclusiveTags)

        assertEquals(false, result)
    }

    @Test
    fun testTagIsIncluded_withMatchingTagsAndNonExclusiveTags_shouldReturnTrue() {
        val filterTags = listOf("tag1", "tag2")
        val buildTags = listOf("tag1", "tag3")
        val exclusiveTags = false

        val result = tagParser.tagIsIncluded(filterTags, buildTags, exclusiveTags)

        assertEquals(true, result)
    }

    @Test
    fun testTagIsIncluded_withNonMatchingTagsAndNonExclusiveTags_shouldReturnTrue() {
        val filterTags = listOf("tag1", "tag2")
        val buildTags = listOf("tag3")
        val exclusiveTags = false

        val result = tagParser.tagIsIncluded(filterTags, buildTags, exclusiveTags)

        assertEquals(false, result)
    }
}
