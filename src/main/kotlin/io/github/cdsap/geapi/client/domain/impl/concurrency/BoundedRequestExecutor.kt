package io.github.cdsap.geapi.client.domain.impl.concurrency

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

internal object BoundedRequestExecutor {
    suspend fun <T, R> execute(
        items: List<T>,
        concurrentCalls: Int,
        transform: suspend (T) -> R,
    ): List<R> =
        coroutineScope {
            val semaphore = Semaphore(concurrentCalls)
            items.map { item ->
                async {
                    semaphore.withPermit {
                        transform(item)
                    }
                }
            }.awaitAll()
        }
}
