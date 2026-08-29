package io.github.cdsap.geapi.client.network

/**
 * Thrown when a Develocity / GE API HTTP response cannot be treated as successful JSON.
 *
 * CLI authors should catch this type to surface actionable auth/URL errors instead of
 * low-level JSON deserialization failures.
 */
class GeApiHttpException(
    val statusCode: Int,
    val requestUrl: String,
    val bodyPreview: String,
    message: String = buildMessage(statusCode, requestUrl, bodyPreview),
    cause: Throwable? = null,
) : Exception(message, cause) {
    companion object {
        internal const val BODY_PREVIEW_LIMIT = 512

        fun buildMessage(
            statusCode: Int,
            requestUrl: String,
            bodyPreview: String,
        ): String {
            val hint = hintForStatus(statusCode)
            val preview =
                if (bodyPreview.isBlank()) {
                    "(empty body)"
                } else {
                    bodyPreview
                }
            return buildString {
                append("GE API HTTP ")
                append(statusCode)
                append(" for ")
                append(requestUrl)
                append(": ")
                append(hint)
                append(" Body preview: ")
                append(preview)
            }
        }

        fun hintForStatus(statusCode: Int): String =
            when (statusCode) {
                401, 403 ->
                    "Unauthorized/forbidden — check --api-key / token permissions."
                404 ->
                    "Not found — check build id and API endpoint/URL."
                in 500..599 ->
                    "Server error — retry later or check Develocity server health."
                else ->
                    "Unexpected response — check server URL and that the API returned JSON."
            }

        fun truncateBody(body: String): String {
            val normalized = body.replace("\r\n", "\n").trim()
            return if (normalized.length <= BODY_PREVIEW_LIMIT) {
                normalized
            } else {
                normalized.take(BODY_PREVIEW_LIMIT) + "…"
            }
        }
    }
}
