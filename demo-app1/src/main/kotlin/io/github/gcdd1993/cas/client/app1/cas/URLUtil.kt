package io.github.gcdd1993.cas.client.app1.cas

import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

internal object URLUtil {
    fun parseQuery(query: String): Map<String, List<String>> {
        return query.split("&").toTypedArray()
            .filter { it.isNotEmpty() }
            .map { it.split("=").toTypedArray() }
            .groupBy(
                keySelector = { urlEncode(it[0]) },
                valueTransform = {
                    if (it.size > 1) urlEncode(it[1]) else ""
                })
    }

    fun urlEncode(content: String): String {
        return try {
            URLEncoder.encode(content, StandardCharsets.UTF_8.name())
        } catch (e: UnsupportedEncodingException) {
            throw IllegalStateException("Non-existing encoding UTF-8, which shouldn't happen", e)
        }
    }

    fun urlDecode(content: String): String {
        return try {
            URLDecoder.decode(content, StandardCharsets.UTF_8.name())
        } catch (e: UnsupportedEncodingException) {
            throw IllegalStateException("Non-existing encoding UTF-8, which shouldn't happen", e)
        }
    }
}