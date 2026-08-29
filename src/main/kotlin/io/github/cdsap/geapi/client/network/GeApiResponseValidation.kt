package io.github.cdsap.geapi.client.network

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess

@PublishedApi
internal object GeApiResponseValidation {
    private val sensitiveQueryParam =
        Regex(
            """(?i)([?&])(access[_-]?token|api[_-]?key|token|key|auth|password|secret)=([^&]*)""",
        )

    fun redactUrl(url: String): String {
        val withoutUserInfo =
            url.replace(Regex("""(https?://)([^/@]+)@"""), "$1REDACTED@")
        return sensitiveQueryParam.replace(withoutUserInfo) { match ->
            "${match.groupValues[1]}${match.groupValues[2]}=REDACTED"
        }
    }

    fun isJsonContentType(contentType: ContentType?): Boolean {
        if (contentType == null) {
            return true
        }
        return contentType.match(ContentType.Application.Json) ||
            contentType.contentSubtype.contains("json", ignoreCase = true)
    }

    fun looksLikeJson(body: String): Boolean {
        val trimmed = body.trimStart()
        if (trimmed.isEmpty()) {
            return false
        }
        val first = trimmed[0]
        return first == '{' || first == '['
    }

    fun validateSuccessfulJsonResponse(
        status: HttpStatusCode,
        requestUrl: String,
        contentType: ContentType?,
        bodyText: String,
    ) {
        val safeUrl = redactUrl(requestUrl)
        val preview = GeApiHttpException.truncateBody(bodyText)

        if (!status.isSuccess()) {
            throw GeApiHttpException(
                statusCode = status.value,
                requestUrl = safeUrl,
                bodyPreview = preview,
            )
        }

        if (!isJsonContentType(contentType) || !looksLikeJson(bodyText)) {
            val contentTypeLabel = contentType?.toString() ?: "missing"
            throw GeApiHttpException(
                statusCode = status.value,
                requestUrl = safeUrl,
                bodyPreview = preview,
                message =
                    GeApiHttpException.buildMessage(status.value, safeUrl, preview) +
                        " (Content-Type: $contentTypeLabel; expected JSON object/array)",
            )
        }
    }
}
