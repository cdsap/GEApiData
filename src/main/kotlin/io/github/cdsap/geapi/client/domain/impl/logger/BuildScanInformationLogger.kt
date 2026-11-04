package io.github.cdsap.geapi.client.domain.impl.logger

import io.github.cdsap.geapi.client.model.Scan
import java.text.SimpleDateFormat
import java.util.Date

internal object BuildScanInformationLogger {
    fun log(
        buildScans: List<Scan>,
        logger: Logger,
    ) {
        if (buildScans.isNotEmpty()) {
            val dateInit = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date(buildScans.first().availableAt))
            logger.log("")
            logger.log("Date first Build scan processed: $dateInit")
            val dateEnd = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date(buildScans.last().availableAt))
            logger.log("Date last Build scan processed: $dateEnd")
        } else {
            logger.log("")
            logger.log("No build scans found")
        }
    }
}
