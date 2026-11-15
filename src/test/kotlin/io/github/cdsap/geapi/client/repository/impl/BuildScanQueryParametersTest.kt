package io.github.cdsap.geapi.client.repository.impl

import io.github.cdsap.geapi.client.model.Filter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BuildScanQueryParametersTest {
    private val parameters = BuildScanQueryParameters()

    @Test
    fun `uses maxBuilds below 1000`() {
        assertEquals(
            "maxBuilds=50&reverse=true",
            parameters.build(Filter(maxBuilds = 50), buildId = null),
        )
    }

    @Test
    fun `caps maxBuilds above 1000`() {
        assertEquals(
            "maxBuilds=1000&reverse=true",
            parameters.build(Filter(maxBuilds = 2500), buildId = null),
        )
    }

    @Test
    fun `uses fromInstant when buildId is absent`() {
        assertEquals(
            "fromInstant=1766178689570&maxBuilds=50&reverse=true",
            parameters.build(
                Filter(maxBuilds = 50, fromInstant = 1766178689570),
                buildId = null,
            ),
        )
    }

    @Test
    fun `fromBuild takes precedence over fromInstant`() {
        assertEquals(
            "fromBuild=prev-build&maxBuilds=200&reverse=true",
            parameters.build(
                Filter(maxBuilds = 200, fromInstant = 1766178689570),
                buildId = "prev-build",
            ),
        )
    }

    @Test
    fun `omits optional advanced query`() {
        assertEquals(
            "fromBuild=prev-build&maxBuilds=200&reverse=true",
            parameters.build(Filter(maxBuilds = 200), buildId = "prev-build"),
        )
    }

    @Test
    fun `appends advanced query after reverse`() {
        assertEquals(
            "fromBuild=prev-build&maxBuilds=1000&reverse=true&query=tag:\"ci\"",
            parameters.build(
                Filter(maxBuilds = 1500, fromInstant = 1766178689570),
                buildId = "prev-build",
                query = "tag:\"ci\"",
            ),
        )
    }
}
