package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.domain.impl.filter.FilterBuildScanAdvancedSearch
import io.github.cdsap.geapi.client.model.Filter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FilterBuildScanAdvancedSearchTest {

    @Test
    fun testFilterWithOnlyTags() {
        val filter = Filter(
            tags = listOf("tag1", "tag2", "tag3"),
            exclusiveTags = false
        )

        val filterBuildScan = FilterBuildScanAdvancedSearch()
        val queryString = filterBuildScan.filter(filter)

        val expectedQueryString = "(tag:tag1%20OR%20tag:tag2%20OR%20tag:tag3)"

        assertEquals(expectedQueryString, queryString)
    }

    @Test
    fun testFilterWithAllFilterOptions() {
        val filter = Filter(
            project = "myProject",
            includeFailedBuilds = false,
            tags = listOf("tag1", "tag2", "tag3"),
            exclusiveTags = true,
            requestedTask = "task123",
            user = "john_doe"
        )

        val filterBuildScan = FilterBuildScanAdvancedSearch()
        val queryString = filterBuildScan.filter(filter)

        val expectedQueryString =
            "project:myProject%20(tag:tag1%20AND%20tag:tag2%20AND%20tag:tag3)%20buildOutcome:succeeded%20user:john_doe%20requested:task123"

        assertEquals(expectedQueryString, queryString)
    }

    @Test
    fun testEscapeTasksWithColons() {
        val filter = Filter(
            project = "myProject",
            includeFailedBuilds = false,
            tags = listOf("tag1", "tag2", "tag3"),
            exclusiveTags = true,
            requestedTask = ":app:compile",
            user = "john_doe"
        )

        val filterBuildScan = FilterBuildScanAdvancedSearch()
        val queryString = filterBuildScan.filter(filter)

        val expectedQueryString =
            "project:myProject%20(tag:tag1%20AND%20tag:tag2%20AND%20tag:tag3)%20buildOutcome:succeeded%20user:john_doe%20requested:\\:app\\:compile"

        assertEquals(expectedQueryString, queryString)
    }

    @Test
    fun testFilterWithOnlyProject() {
        val filter = Filter(project = "myProject")

        val filterBuildScan = FilterBuildScanAdvancedSearch()
        val queryString = filterBuildScan.filter(filter)

        val expectedQueryString = "project:myProject"

        assertEquals(expectedQueryString, queryString)
    }

    @Test
    fun testFilterWithOnlyIncludeFailedBuilds() {
        val filter = Filter(includeFailedBuilds = true)

        val filterBuildScan = FilterBuildScanAdvancedSearch()
        val queryString = filterBuildScan.filter(filter)

        val expectedQueryString = ""

        assertEquals(expectedQueryString, queryString)
    }

    @Test
    fun testFilterNegativeTags() {
        val filter = Filter(exclusiveTags = true, tags = listOf("ci", "!main"))

        val filterBuildScan = FilterBuildScanAdvancedSearch()
        val queryString = filterBuildScan.filter(filter)

        val expectedQueryString = "(tag:ci%20AND%20-tag:main)"

        assertEquals(expectedQueryString, queryString)
    }

    @Test
    fun testFilterNegativeTagsWithoutExclusiveTags() {
        val filter = Filter(exclusiveTags = false, tags = listOf("ci", "!main"))

        val filterBuildScan = FilterBuildScanAdvancedSearch()
        val queryString = filterBuildScan.filter(filter)

        val expectedQueryString = "(tag:ci%20AND%20-tag:main)"

        assertEquals(expectedQueryString, queryString)
    }

    @Test
    fun testFilterNegativeTagsOnlyStripsFirstCharacter() {
        val filter = Filter(exclusiveTags = true, tags = listOf("ci", "!!main"))

        val filterBuildScan = FilterBuildScanAdvancedSearch()
        val queryString = filterBuildScan.filter(filter)

        val expectedQueryString = "(tag:ci%20AND%20-tag:!main)"

        assertEquals(expectedQueryString, queryString)
    }
}
