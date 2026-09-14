package io.github.cdsap.geapi.client.domain.impl

import kotlinx.coroutines.sync.Semaphore

internal suspend fun <T> Semaphore.executeWithPermit(action: suspend () -> T): T {
    acquire()
    try {
        return action()
    } finally {
        release()
    }
}
