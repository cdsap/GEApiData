package io.github.cdsap.geapi.client.domain.impl.logger

import io.github.cdsap.geapi.client.model.ClientType
import io.github.cdsap.geapi.client.model.Scan
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.test.Test
import kotlin.test.assertEquals

class BuildScanSummaryLoggerTest {
    @Test
    fun logsFirstAndLastScanDatesWithTheExistingFormat() {
        val firstAvailableAt = 0L
        val lastAvailableAt = 1_000L

        val output = captureOutput {
            BuildScanSummaryLogger.log(
                listOf(
                    Scan("first", "gradle", firstAvailableAt),
                    Scan("last", "gradle", lastAvailableAt),
                ),
                Logger(ClientType.CLI),
            )
        }

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        assertEquals(
            "\nDate first Build scan processed: ${dateFormat.format(Date(firstAvailableAt))}\n" +
                "Date last Build scan processed: ${dateFormat.format(Date(lastAvailableAt))}\n",
            output,
        )
    }

    @Test
    fun logsWhenNoBuildScansAreFound() {
        val output = captureOutput {
            BuildScanSummaryLogger.log(emptyList(), Logger(ClientType.CLI))
        }

        assertEquals("\nNo build scans found\n", output)
    }

    @Test
    fun remainsSilentForNonCliClients() {
        val output = captureOutput {
            BuildScanSummaryLogger.log(
                listOf(Scan("scan", "gradle", 0L)),
                Logger(ClientType.API),
            )
        }

        assertEquals("", output)
    }

    private fun captureOutput(block: () -> Unit): String {
        val output = ByteArrayOutputStream()
        val originalOut = System.out
        System.setOut(PrintStream(output))
        try {
            block()
        } finally {
            System.setOut(originalOut)
        }
        return output.toString()
    }
}
