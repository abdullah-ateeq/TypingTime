package com.example.typingtime.analyzer

object ParagraphDetector {

    /**
     * Splits text into meaningful paragraphs separated by one or more blank lines.
     */
    fun detectParagraphs(text: String): List<String> {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return emptyList()

        // Split on one or more blank lines (double newlines or lines with only spaces)
        val parts = trimmed.split(Regex("(\\r?\\n\\s*){2,}"))
        return parts.map { it.trim() }.filter { it.isNotEmpty() }
    }

    fun countParagraphs(text: String): Int {
        return detectParagraphs(text).size
    }
}
