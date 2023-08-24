package io.github.cdsap.geapi.client.domain.impl

import io.github.cdsap.geapi.client.model.ClientType

class Logger(private val clientType: ClientType) {

    fun log(message: String) {
        if (clientType == ClientType.CLI) {
            println(message)
        }
    }
}
