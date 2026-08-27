package io.github.cdsap.geapi

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class BuildWorkflowTest {
    @Test
    fun buildWorkflow_includesBuildProcessWatcherBeforeGradleTests() {
        val workflow = File(".github/workflows/build.yaml").readText()

        assertTrue(
            workflow.contains("uses: cdsap/build-process-watcher@v0.6.2"),
            "Expected build-process-watcher action in GHA gradle build workflow",
        )
        assertTrue(
            workflow.contains("remote_monitoring: 'true'"),
            "Expected remote_monitoring enabled for build-process-watcher",
        )
        assertTrue(
            workflow.contains("export_to_bigquery: 'true'"),
            "Expected BigQuery export enabled for build-process-watcher",
        )

        val watcherIndex = workflow.indexOf("uses: cdsap/build-process-watcher@v0.6.2")
        val gradleTestIndex = workflow.indexOf("run: ./gradlew test")
        assertTrue(watcherIndex >= 0, "build-process-watcher step missing")
        assertTrue(gradleTestIndex >= 0, "gradle test step missing")
        assertTrue(
            watcherIndex < gradleTestIndex,
            "build-process-watcher must run before ./gradlew test",
        )
    }
}
