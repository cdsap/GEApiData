package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.domain.impl.progress.ProgressFeedback
import io.github.cdsap.geapi.client.model.Filter
import io.github.cdsap.geapi.client.model.ScanWithAttributes
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

internal object BuildScanBatchProcessor {
    suspend fun <T> process(
        builds: List<ScanWithAttributes>,
        filter: Filter,
        predicate: (ScanWithAttributes) -> Boolean = { true },
        transform: suspend (ScanWithAttributes) -> T,
    ): List<T> {
        val progressFeedback = ProgressFeedback(filter.clientType, builds.size)
        val semaphore = Semaphore(filter.concurrentCallsConservative)

        progressFeedback.init()

        return coroutineScope {
            builds.filter(predicate).map { build ->
                async {
                    semaphore.withPermit {
                        transform(build).also {
                            progressFeedback.update()
                        }
                    }
                }
            }.awaitAll()
        }
    }
}
