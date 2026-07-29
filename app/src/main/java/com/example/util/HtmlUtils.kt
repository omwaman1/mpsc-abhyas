package com.example.util

import androidx.core.text.HtmlCompat

fun String.cleanHtml(): String {
    if (this.isEmpty()) return ""
    return try {
        val unescaped = HtmlCompat.fromHtml(
            this,
            HtmlCompat.FROM_HTML_MODE_LEGACY
        ).toString()
        unescaped
            .replace(Regex("<[^>]*>"), "")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace(Regex("\\s+"), " ")
            .trim()
    } catch (e: Exception) {
        this.replace(Regex("<[^>]*>"), "")
            .replace("&nbsp;", " ")
            .trim()
    }
}
