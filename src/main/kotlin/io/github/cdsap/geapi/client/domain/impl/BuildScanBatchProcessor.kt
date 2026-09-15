package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.domain.impl.concurrency.BoundedRequestExecutor
import io.github.cdsap.geapi.client.domain.impl.progress.ProgressFeedback
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.model.ScanWithAttributes

internal object BuildScanBatchProcessor {
    suspend fun <T> process(
        builds: List<ScanWithAttributes>,
        filter: Filter,
        predicate: (ScanWithAttributes) -> Boolean = { true },
        transform: suspend (ScanWithAttributes) -> T,
    ): List<T> {
        val progressFeedback = ProgressFeedback(filter.clientType, builds.size)

        progressFeedback.init()

        return BoundedRequestExecutor.execute(
            items = builds.filter(predicate),
            concurrentCalls = filter.concurrentCallsConservative,
        ) { build ->
            transform(build).also {
                progressFeedback.update()
            }
        }
    }
}
