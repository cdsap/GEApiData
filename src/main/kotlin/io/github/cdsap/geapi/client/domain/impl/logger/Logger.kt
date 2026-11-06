package io.github.cdsap.geapi.client.domain.impl.logger

import io.github.cdsap.geapi.client.model.ClientType
import io.github.cdsap.geapi.client.model.Scan
import java.text.SimpleDateFormat
import java.util.Date

class Logger(private val clientType: ClientType) {
    fun log(message: String) {
        if (clientType == ClientType.CLI) {
            println(message)
        }
    }

    internal fun logBuildScanInformation(buildScans: List<Scan>) {
        if (buildScans.isNotEmpty()) {
            val dateInit = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date(buildScans.first().availableAt))
            log("")
            log("Date first Build scan processed: $dateInit")
            val dateEnd = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date(buildScans.last().availableAt))
            log("Date last Build scan processed: $dateEnd")
        } else {
            log("")
            log("No build scans found")
        }
    }
}
