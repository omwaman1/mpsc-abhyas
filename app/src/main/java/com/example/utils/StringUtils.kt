package com.example.utils

fun String?.cleanHtml(): String {
    if (this.isNullOrBlank()) return ""
    val textWithBreaks = this
        .replace(Regex("(?i)<br\\s*/?>"), "\n")
        .replace(Regex("(?i)</p>"), "\n")
        .replace(Regex("(?i)</tr>"), "\n")
        .replace(Regex("(?i)</td>"), "   ")
        .replace(Regex("(?i)</th>"), "   ")

    return textWithBreaks
        .replace(Regex("<[^>]*>"), "")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace(Regex("[ \\t]+"), " ")
        .replace(Regex("\n{3,}"), "\n\n")
        .trim()
}
