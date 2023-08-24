package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.model.ClientType
import io.github.cdsap.geapi.client.progressbar.ProgressBar

class ProgressFeedback(
    private val clientType: ClientType,
    private val size: Int
) {
    private val progressBar: ProgressBar?
    private var i = 0

    init {
        if (clientType == ClientType.CLI) {
            progressBar = ProgressBar()
        } else {
            progressBar = null
        }
    }

    fun init() {
        if (clientType == ClientType.CLI) {
            progressBar?.update(0, size)
        }
    }

    fun update() {
        if (clientType == ClientType.CLI) {
            progressBar?.update(i++, size)
        }
    }
}
